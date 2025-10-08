package ru.svifty7.services.domain.rudagames.exception;

public class AuthException extends RuntimeException {
    public AuthException() {
        super("auth failed");
    }
    public AuthException(String message) {
        super(String.format("auth failed: %s", message));
    }
}
