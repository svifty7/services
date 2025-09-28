package ru.svifty7.services.domain.rudagames.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.svifty7.services.domain.rudagames.service.EventsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTasks {

    private final EventsService eventsService;

    @Scheduled(cron = "0 7 18 13-19 * *")
    public void announceEvent() {
        try {
            eventsService.announceEvent();
        } catch (Exception ex) {
            log.error("error while announce events: {}", ex.getMessage(), ex);
        }
    }

    @Scheduled(cron = "0 23 13-19 * * *")
    public void notifyAboutRegistration() {
        try {
            eventsService.announceEvent();
        } catch (Exception ex) {
            log.error("error while notify about registration: {}", ex.getMessage(), ex);
        }
    }

}
