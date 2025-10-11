package ru.svifty7.services.domain.rudagames.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svifty7.services.domain.rudagames.dto.AcceptedGame;
import ru.svifty7.services.domain.rudagames.dto.VkUserInfo;
import ru.svifty7.services.domain.rudagames.dto.callback.AcceptedGameInvitePayload;
import ru.svifty7.services.domain.rudagames.entity.AcceptedGameEntity;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;
import ru.svifty7.services.domain.rudagames.entity.TeamEntity;
import ru.svifty7.services.domain.rudagames.mapper.AcceptedGamesMapper;
import ru.svifty7.services.domain.rudagames.repository.AcceptedGamesRepository;
import ru.svifty7.services.domain.rudagames.repository.EventsRepository;
import ru.svifty7.services.domain.rudagames.repository.TeamsRepository;
import ru.svifty7.services.domain.rudagames.util.StringUtil;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcceptedGamesService {

    private final EventsRepository eventsRepository;
    private final VkService vkService;
    private final EventsService eventsService;
    private final RudagamesService rudagamesService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd MMMM yyyy HH:mm").withZone(ZoneId.of("Europe/Kirov"));
    private final AcceptedGamesMapper acceptedGamesMapper;
    private final AcceptedGamesRepository acceptedGamesRepository;
    private final TeamsRepository teamsRepository;
    private final ObjectMapper objectMapper;
    private final VkCacheService vkCacheService;

    @Transactional
    public void updateAcceptedGames() throws IOException {
        List<AcceptedGame> acceptedGames = rudagamesService.loadCurrentAcceptedGames();
        if (acceptedGames.isEmpty()) {
            log.info("no accepted games to update");
            return;
        }

        eventsService.updateEvents();

        Set<UUID> eventUuids = acceptedGames.stream()
                .map(AcceptedGame::eventRecordId)
                .collect(Collectors.toSet());

        Set<Integer> teamIds = acceptedGames.stream()
                .map(AcceptedGame::teamId)
                .collect(Collectors.toSet());

        Map<UUID, EventEntity> eventsMap = eventsRepository
                .findByUuidList(eventUuids.stream().toList())
                .stream()
                .collect(Collectors.toMap(EventEntity::getUuid, e -> e));

        Map<Integer, TeamEntity> teamsMap = teamsRepository
                .findAllById(teamIds)
                .stream()
                .collect(Collectors.toMap(TeamEntity::getId, t -> t));

        List<AcceptedGameEntity> acceptedGameEntities = acceptedGames.stream()
                .filter(ag -> {
                    boolean teamExists = teamsMap.containsKey(ag.teamId());
                    if (!teamExists) {
                        log.warn("skipping accepted game: team [{}] not found for event [{}]",
                                ag.teamId(), ag.eventRecordId());
                    }
                    return teamExists;
                })
                .map(ag -> mapToAcceptedGameEntities(ag, eventsMap, teamsMap))
                .toList();

        acceptedGamesRepository.saveAll(acceptedGameEntities);
        log.info("updated {} accepted games", acceptedGameEntities.size());
    }

    @Transactional
    public void notifyAboutRegistration() throws IOException {
        updateAcceptedGames();

        acceptedGamesRepository.findClosestAcceptedAndNotNotified()
                .ifPresentOrElse(
                        this::sendRegistrationNotification,
                        () -> log.info("no accepted games available for notification")
                );
    }

    private void sendRegistrationNotification(AcceptedGameEntity acceptedGame) {
        EventEntity event = acceptedGame.getEvent();
        try {
            Integer messageId = vkService.sendMessageWithPhotoAndKeyboard(
                    getRegistrationNotifyMessage(event, acceptedGame),
                    event.getImageUrl(),
                    createCallbackKeyboard()
            );

            acceptedGame.setNotifiedAt(Instant.now());
            acceptedGame.setMessageId(messageId);
            acceptedGame.setPlayersCount(0);
            acceptedGamesRepository.save(acceptedGame);

            log.info("registration notification sent for team [{}] and event [{}], message id: {}",
                    acceptedGame.getTeam().getId(), event.getUuid(), messageId);
        } catch (Exception e) {
            log.error("failed to notify about registration for event [{}]: {}",
                    event.getUuid(), e.getMessage(), e);
        }
    }

    private Keyboard createCallbackKeyboard() {
        Keyboard keyboard = new Keyboard();
        keyboard.setInline(true);
        keyboard.setOneTime(false);
        keyboard.setButtons(
                Stream.of(KeyboardButtonColor.POSITIVE, KeyboardButtonColor.NEGATIVE)
                        .map(color -> IntStream.rangeClosed(1, 5)
                                .mapToObj(i -> createButton(color, i))
                                .toList())
                        .toList()
        );

        return keyboard;
    }

    private KeyboardButton createButton(KeyboardButtonColor color, int count) {
        boolean isPositive = color == KeyboardButtonColor.POSITIVE;

        KeyboardButton button = new KeyboardButton();
        KeyboardButtonAction action = new KeyboardButtonAction();
        button.setColor(color);
        button.setAction(action);
        action.setType(TemplateActionTypeNames.CALLBACK);
        action.setLabel((isPositive ? "+" : "-") + count);

        try {
            AcceptedGameInvitePayload payloadDto = new AcceptedGameInvitePayload(isPositive ? "add" : "subtract", count);
            action.setPayload(objectMapper.writeValueAsString(payloadDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return button;
    }

    @Transactional
    public void handleAcceptedGameInviteCallback(String eventId, Integer userId, Integer peerId, Integer msgId, AcceptedGameInvitePayload payload) throws ClientException, ApiException {
        log.info("accepted game invite handler: eventId={}, userId={}, peerId={}, msgId={}, payload={}", eventId, userId, peerId, msgId, payload);

        AcceptedGameEntity acceptedGame = acceptedGamesRepository.findByMessageId(msgId).orElseThrow();
        int oldPlayersCount = acceptedGame.getPlayersCount() != null ? acceptedGame.getPlayersCount() : 0;
        AcceptedGameEntity updated = updatePlayersCount(acceptedGame, payload);
        int newPlayersCount = updated.getPlayersCount() != null ? updated.getPlayersCount() : 0;

        VkUserInfo user = vkCacheService.getUserInfo(userId);
        String userMention = vkService.createMention(user);

        if (newPlayersCount > oldPlayersCount) {
            String verb = StringUtil.getGenderString(user.sex(), "добавил", "добавила");
            String playerWord = StringUtil.getPlural(payload.count(), new String[]{"игрока", "игроков", "игроков"});

            vkService.sendMessage(String.format(
                    "🎉 %s %s %d %s на игру %s!\n\n✨ Уже согласились: %d из %d возможных.\n%s",
                    userMention,
                    verb,
                    payload.count(),
                    playerWord,
                    DATE_TIME_FORMATTER.format(updated.getEvent().getPlayAt()),
                    newPlayersCount,
                    updated.getEvent().getMaxPlayersInTeam(),
                    getMotivationalMessage(newPlayersCount, oldPlayersCount, updated.getEvent().getMaxPlayersInTeam())
            ));

        } else if (oldPlayersCount > newPlayersCount) {
            String verb = StringUtil.getGenderString(user.sex(), "отменил", "отменила");
            int diff = oldPlayersCount - newPlayersCount;
            String playerWord = StringUtil.getPlural(diff, new String[]{"игрока", "игроков", "игроков"});

            vkService.sendMessage(String.format(
                    "💔 %s %s участие %d %s в игре %s\n\n👥 Осталось игроков: %d из %d возможных.\n%s",
                    userMention,
                    verb,
                    diff,
                    playerWord,
                    DATE_TIME_FORMATTER.format(updated.getEvent().getPlayAt()),
                    newPlayersCount,
                    updated.getEvent().getMaxPlayersInTeam(),
                    getMotivationalMessage(newPlayersCount, oldPlayersCount, updated.getEvent().getMaxPlayersInTeam())
            ));

        } else {
            vkService.sendCallbackAnswerToSnackbar(eventId, userId, peerId,
                    "🤨 Хм... количество игроков осталось прежним. Возможно какая-то ошибка...");
        }
    }

    private String getMotivationalMessage(int current, int prev, int max) {
        if (current == 0 && prev > 0) return "Пу-пу-пу...";
        if (current == max) return "🎯 Команда в сборе!";
        if (current > max) return String.format("🔥 В резерве %d", current - max);
        if (current >= max * 0.8) return "💯 Почти укомплектованы!";
        if (current >= max * 0.5) return "👍 Половина уже есть, продолжаем!";
        return "🎲 Ждём ещё желающих!";
    }


    @NotNull
    private AcceptedGameEntity updatePlayersCount(AcceptedGameEntity acceptedGame, AcceptedGameInvitePayload payload) {
        int currentCount = acceptedGame.getPlayersCount() != null
                ? acceptedGame.getPlayersCount()
                : 0;

        switch (payload.action()) {
            case "add" -> acceptedGame.setPlayersCount(currentCount + payload.count());
            case "subtract" -> acceptedGame.setPlayersCount(Math.max(0, currentCount - payload.count()));
            default -> throw new IllegalArgumentException("Unknown action: " + payload.action());
        }

        return acceptedGamesRepository.save(acceptedGame);
    }

    private AcceptedGameEntity mapToAcceptedGameEntities(
            AcceptedGame acceptedGame,
            Map<UUID, EventEntity> eventsMap,
            Map<Integer, TeamEntity> teamsMap) {
        return acceptedGamesRepository.findByEventUuid(acceptedGame.eventRecordId())
                .map(existing -> acceptedGamesMapper.toUpdate(acceptedGame, existing, eventsMap, teamsMap))
                .orElseGet(() -> acceptedGamesMapper.toEntity(acceptedGame, eventsMap, teamsMap));
    }

    private String getRegistrationNotifyMessage(EventEntity event, AcceptedGameEntity acceptedGame) {
        StringBuilder sb = new StringBuilder();

        sb.append("🎯 ")
                .append(acceptedGame.getTeam().getName())
                .append(", перекличка!\n\n");
        sb.append("🎲️ ").append(event.getProduct().getName());

        if (event.getTag() != null && !event.getTag().isBlank()) {
            sb.append(" ").append(event.getTag());
        }

        sb.append("\n")
                .append("📌 ").append(event.getName().replaceFirst("(?i)" + event.getProduct().getName() + "\\s*", ""));
        sb.append('\n')
                .append("📅 Дата и время: ")
                .append(DATE_TIME_FORMATTER.format(event.getPlayAt()))
                .append('\n')
                .append("👥 Мест: ")
                .append(event.getMaxPlayersInTeam())
                .append('\n')
                .append("🧮 Мин. игроков: ")
                .append(event.getMinPlayersInTeam());

        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            sb.append("\n\n")
                    .append(event.getDescription().strip());
        }

        return sb.toString();
    }
}
