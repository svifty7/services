package ru.svifty7.services.domain.rudagames.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rudagames")
public class RudagamesConfig {

    private Api api;
    private Location location;
    private Vk vk;

    @Data
    public static class Api {
        private String baseUrl;
        private String signIn;
        private String events;
        private String locations;
        private String cities;
        private String countries;
        private String acceptedGames;
        private String products;
    }

    @Data
    public static class Location {
        private Integer city;
        private Integer country;
    }

    @Data
    public static class Vk {
        private Integer teamChatId;
    }

    public String getSignInUrl() {
        return this.api.baseUrl + this.api.signIn;
    }

    public String getAcceptedGamesUrl() {
        return this.api.baseUrl + this.api.acceptedGames;
    }

    public String getEventsUrl() {
        return String.format("%s%s/%s", this.api.baseUrl, this.api.events, this.location.city);
    }

}
