package ru.svifty7.services.domain.rudagames.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;
import ru.svifty7.services.domain.rudagames.entity.ProductEntity;
import ru.svifty7.services.domain.rudagames.mapper.EventsMapper;
import ru.svifty7.services.domain.rudagames.repository.EventsRepository;
import ru.svifty7.services.domain.rudagames.repository.ProductsRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

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
            List<CityEvent> cityEvents = rudagamesService.loadActualEvents();

            Map<Integer, ProductEntity> productsMap = productsRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(ProductEntity::getId, p -> p));

            List<CityEvent> filteredEvents = cityEvents.stream()
                    .filter(cityEvent -> productsMap.containsKey(cityEvent.productId()))
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
                    event = eventsMapper.toUpdate(cityEvent, event, productsMap);
                } else {
                    event = eventsMapper.toEntity(cityEvent, productsMap);
                }

                eventsToSave.add(event);
            }

            eventsRepository.saveAll(eventsToSave);
            log.info("update events completed successfully, saved {} events", eventsToSave.size());
        } catch (Exception e) {
            log.error("failed to update events: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void announceEvent() {
        updateEvents();

        eventsRepository.findLastNotAcceptedAndIsNotAnnounced()
                .ifPresentOrElse(
                        this::sendAnnouncement,
                        () -> log.info("no events available for announcement")
                );
    }

    private void sendAnnouncement(EventEntity event) {
        try {
            vkService.sendMessageWithPhoto(getAnnounceMessage(event), event.getImageUrl());
            event.setAnnouncedAt(Instant.now());
            eventsRepository.save(event);
            log.info("event [{}] announced successfully", event.getUuid());
        } catch (Exception e) {
            log.error("failed to announce event [{}]: {}", event.getUuid(), e.getMessage(), e);
        }
    }

    private String getAnnounceMessage(EventEntity e) {
        StringBuilder sb = new StringBuilder();

        sb.append("📢 Анонс предстоящей игры!\n\n");

        sb.append("🎲 ").append(e.getProduct().getName());

        if (e.getTag() != null && !e.getTag().isBlank()) {
            sb.append(" ").append(e.getTag());
        }

        sb.append("\n").append("📌 ").append(e.getName());
        sb.append('\n').append("📅 ").append(DATE_TIME_FORMATTER.format(e.getPlayAt())).append('\n');

        if (e.getDescription() != null && !e.getDescription().isBlank()) {
            sb.append('\n').append(e.getDescription().strip());
        }

        return sb.toString();
    }

}
