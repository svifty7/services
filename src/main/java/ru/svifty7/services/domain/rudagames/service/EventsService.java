package ru.svifty7.services.domain.rudagames.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svifty7.services.domain.rudagames.dto.AcceptedGame;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;
import ru.svifty7.services.domain.rudagames.entity.ProductEntity;
import ru.svifty7.services.domain.rudagames.entity.TeamEntity;
import ru.svifty7.services.domain.rudagames.exception.NoEventsForNotifyException;
import ru.svifty7.services.domain.rudagames.exception.UpdateEventsException;
import ru.svifty7.services.domain.rudagames.mapper.EventsMapper;
import ru.svifty7.services.domain.rudagames.repository.EventsRepository;
import ru.svifty7.services.domain.rudagames.repository.ProductsRepository;
import ru.svifty7.services.domain.rudagames.repository.TeamsRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

    private final TeamsRepository teamsRepository;
    private final EventsRepository eventsRepository;
    private final ProductsRepository productsRepository;

    private final EventsMapper eventsMapper;

    private final VkService vkService;
    private final RudagamesService rudagamesService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd MMMM yyyy HH:mm").withZone(ZoneId.of("Europe/Kirov"));

    @Transactional
    public void updateEvents() {
        try {
            List<CityEvent> cityEvents = rudagamesService.getEvents();

            List<Integer> availableProductIds = productsRepository.findAll().stream()
                    .map(ProductEntity::getId)
                    .toList();

            List<CityEvent> filteredEvents = cityEvents.stream()
                    .filter(cityEvent -> availableProductIds.contains(cityEvent.productId()))
                    .toList();

            log.info("upcoming events count: {}", filteredEvents.size());

            List<UUID> uuidList = filteredEvents.stream()
                    .map(CityEvent::eventRecordId)
                    .toList();

            List<EventEntity> existingEvents = eventsRepository.findByUuidList(uuidList);

            Map<UUID, EventEntity> existingMap = existingEvents.stream()
                    .collect(Collectors.toMap(EventEntity::getUuid, Function.identity()));

            List<EventEntity> eventsToSave = new ArrayList<>();

            for (CityEvent cityEvent : filteredEvents) {
                UUID uuid = cityEvent.eventRecordId();
                EventEntity event = existingMap.get(uuid);

                if (event != null) {
                    eventsMapper.updateEntity(cityEvent, event);
                } else {
                    event = eventsMapper.toEntity(cityEvent);
                }

                eventsToSave.add(event);
            }

            List<AcceptedGame> acceptedGames = rudagamesService.getAcceptedGames();
            log.info("accepted games: {}", acceptedGames.stream().map(AcceptedGame::eventRecordId));

            List<TeamEntity> teams = teamsRepository.findAll();

            for (EventEntity event : eventsToSave) {
                Optional<AcceptedGame> acceptedGame = acceptedGames.stream()
                        .filter(game -> game.eventRecordId().equals(event.getUuid())).findFirst();

                if (acceptedGame.isEmpty()) {
                    event.setAcceptedAt(null);
                    event.setTeam(null);
                } else {
                    event.setAcceptedAt(acceptedGame.get().createdAt());
                    event.setTeam(teams.stream()
                            .filter(team -> team.getId().equals(acceptedGame.get().teamId())).findFirst()
                            .orElse(null));
                }
            }

            eventsRepository.saveAll(eventsToSave);

            log.info("update events is done");
        } catch (Exception e) {
            log.error("update events failed", e);
            throw new UpdateEventsException();
        }
    }


    @Transactional
    public void announceEvent() {
        updateEvents();

        EventEntity event = eventsRepository.findLastNotAcceptedAndIsNotAnnounced()
                .orElseThrow(NoEventsForNotifyException::new);

        try {
            vkService.sendMessageWithPhoto(getAnnounceMessage(event), event.getImageUrl());

            log.info("event with uuid[{}] announced", event.getUuid());

            event.setAnnouncedAt(Instant.now());
            eventsRepository.save(event);

            log.info("announced_at is updated for event with uuid[{}]", event.getUuid());
        } catch (Exception e) {
            log.error("error while announce event: {}", e.getMessage(), e);
        }
    }

    private String getAnnounceMessage(EventEntity e) {
        StringBuilder sb = new StringBuilder();

        sb.append("📢 Анонс предстоящей игры!\n\n");

        sb.append("🎲 ").append(e.getProduct().getName());

        if (e.getTag() != null && !e.getTag().isBlank()) {
            sb.append(e.getTag());
        }

        sb.append("\n").append("📌 ").append(e.getName());
        sb.append('\n').append("📅 ").append(DATE_TIME_FORMATTER.format(e.getPlayAt())).append('\n');

        if (e.getDescription() != null && !e.getDescription().isBlank()) {
            sb.append('\n').append(e.getDescription().strip());
        }

        return sb.toString();
    }

    @Transactional
    public void notifyAboutRegistration() {
        updateEvents();

        EventEntity event = eventsRepository.findLastAcceptedAndIsNotNotified()
                .orElseThrow(NoEventsForNotifyException::new);

        try {
            vkService.sendMessageWithPhoto(getRegistrationNotifyMessage(event), event.getImageUrl());

            log.info("users notified about registration on event with uuid[{}]", event.getUuid());

            event.setNotifiedAt(Instant.now());
            eventsRepository.save(event);

            log.info("notified_at is updated for event with uuid[{}]", event.getUuid());
        } catch (Exception e) {
            log.error("error while notify users about registration: {}", e.getMessage(), e);
        }
    }

    private String getRegistrationNotifyMessage(EventEntity e) {
        StringBuilder sb = new StringBuilder();

        sb.append("🎯 ")
                .append(e.getTeam().getName())
                .append(", перекличка!\n\n");

        sb.append("🎲️ ").append(e.getProduct().getName());

        if (e.getTag() != null && !e.getTag().isBlank()) {
            sb.append(" ").append(e.getTag());
        }

        sb.append("\n")
                .append("📌 ").append(e.getName().replaceFirst("(?i)" + e.getProduct().getName() + "\\s*", ""));

        sb.append('\n')
                .append("📅 Дата и время: ")
                .append(DATE_TIME_FORMATTER.format(e.getPlayAt()))
                .append('\n')
                .append("👥 Мест: ")
                .append(e.getMaxPlayersInTeam())
                .append('\n')
                .append("🧮 Мин. игроков: ")
                .append(e.getMinPlayersInTeam());

        if (e.getDescription() != null && !e.getDescription().isBlank()) {
            sb.append("\n\n")
                    .append(e.getDescription().strip());
        }

        return sb.toString();
    }

}
