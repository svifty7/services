package ru.svifty7.services.domain.rudagames.dto.callback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamRegistrationPayload(
        String action,
        int team
) implements CallbackPayload {

    @Override
    @JsonProperty("type")
    public String type() {
        return "team-registration";
    }
}
