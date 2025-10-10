package ru.svifty7.services.domain.rudagames.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svifty7.services.domain.rudagames.dto.AcceptedGame;
import ru.svifty7.services.domain.rudagames.entity.AcceptedGameEntity;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;
import ru.svifty7.services.domain.rudagames.entity.TeamEntity;
import ru.svifty7.services.domain.rudagames.exception.NoEventsForNotifyException;
import ru.svifty7.services.domain.rudagames.mapper.AcceptedGamesMapper;
import ru.svifty7.services.domain.rudagames.repository.AcceptedGamesRepository;
import ru.svifty7.services.domain.rudagames.repository.EventsRepository;
import ru.svifty7.services.domain.rudagames.repository.TeamsRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional
    public void updateAcceptedGames() throws IOException {
        List<AcceptedGame> acceptedGames = rudagamesService.loadCurrentAcceptedGames();
        if (acceptedGames.isEmpty()) {
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
                .map(ag -> mapToAcceptedGameEntities(ag, eventsMap, teamsMap))
                .toList();

        acceptedGamesRepository.saveAll(acceptedGameEntities);
    }

    private AcceptedGameEntity mapToAcceptedGameEntities(
            AcceptedGame acceptedGame,
            Map<UUID, EventEntity> eventsMap,
            Map<Integer, TeamEntity> teamsMap
    ) {
        return acceptedGamesRepository.findByEventUuid(acceptedGame.eventRecordId())
                .map(existing -> acceptedGamesMapper
                        .toUpdate(acceptedGame, existing, eventsMap, teamsMap))
                .orElseGet(() -> acceptedGamesMapper
                        .toEntity(acceptedGame, eventsMap, teamsMap));
    }



    @Transactional
    public void notifyAboutRegistration() throws IOException {
        updateAcceptedGames();

        AcceptedGameEntity acceptedGame = acceptedGamesRepository.findClosestAcceptedAndNotNotified()
                .orElseThrow(NoEventsForNotifyException::new);

        EventEntity event = acceptedGame.getEvent();

        try {
            vkService.sendMessageWithPhoto(getRegistrationNotifyMessage(event, acceptedGame), event.getImageUrl());

            acceptedGame.setNotifiedAt(Instant.now());
            acceptedGamesRepository.save(acceptedGame);
        } catch (Exception e) {
            log.error("error while notify users about registration: {}", e.getMessage(), e);
        }
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
