package ru.svifty7.services.domain.rudagames.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.svifty7.services.domain.rudagames.service.EventsService;

@RequiredArgsConstructor
@RestController("/api/v1/events")
public class EventsController {

    private final EventsService eventsService;

    @GetMapping("/update-events")
    public void updateEvents() {
        eventsService.updateEvents();
    }

    @GetMapping("/notify-about-registration")
    public void notifyAboutRegistration() {
        eventsService.notifyAboutRegistration();
    }

    @GetMapping("/announce-event")
    public void announceEvent() {
        eventsService.announceEvent();
    }

}
