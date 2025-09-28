package ru.svifty7.services.domain.rudagames.exception;

public class UpdateEventsException extends RuntimeException {
    public UpdateEventsException() {
        super("Update events failed");
    }
}
