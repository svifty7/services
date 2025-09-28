package ru.svifty7.services.domain.rudagames.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApplicationCustomDesign(
        @JsonProperty("bgImage")
        String bgImage,

        @JsonProperty("eventTime")
        CustomColor eventTime,

        @JsonProperty("eventPlace")
        CustomColor eventPlace,

        @JsonProperty("activeTeams")
        ActiveTeams activeTeams,

        @JsonProperty("reservedTeams")
        ReservedTeams reservedTeams
) {
}
