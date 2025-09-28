package ru.svifty7.services.domain.rudagames.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn

) {}
