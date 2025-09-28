package ru.svifty7.services.domain.rudagames.dto;

public record Response(
        int statusCode,
        String error,
        String message
) {
}
