package ru.svifty7.services.domain.rudagames.dto.callback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AcceptedGameInvitePayload(
        String action,
        int count
) implements CallbackPayload {

    @Override
    @JsonProperty("type")
    public String type() {
        return "accepted-game-invite";
    }
}
