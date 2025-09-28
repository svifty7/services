package ru.svifty7.services.domain.rudagames.exception;

public class NoEventsForNotifyException extends RuntimeException {
    public NoEventsForNotifyException() {
        super("no events for notify");
    }
}
