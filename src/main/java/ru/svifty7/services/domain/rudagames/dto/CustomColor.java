package ru.svifty7.services.domain.rudagames.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomColor(
        @JsonProperty("bgColor")
        String bgColor,

        @JsonProperty("fontSize")
        String fontSize,

        @JsonProperty("textColor")
        String textColor
) {
}
