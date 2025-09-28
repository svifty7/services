package ru.svifty7.services.domain.rudagames.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.svifty7.services.domain.rudagames.dto.serializer.MskInstantDeserializer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CityEvent(
        UUID uuid,

        @JsonProperty("event_record_id")
        UUID eventRecordId,
        String place,

        @JsonProperty("distribution_format")
        String distributionFormat,

        @JsonProperty("game_name")
        String gameName,

        @JsonProperty("displayed_game_name")
        String displayedGameName,
        String address,
        String type,
        String time,
        Integer price,

        @JsonProperty("registration_at")
        @JsonDeserialize(using = MskInstantDeserializer.class)
        Instant registrationAt,

        @JsonProperty("played_at")
        @JsonDeserialize(using = MskInstantDeserializer.class)
        Instant playedAt,

        @JsonProperty("game_type")
        String gameType,

        @JsonProperty("parent_type")
        String parentType,

        @JsonProperty("displayed_parent_name")
        String displayedParentName,
        String rating,

        @JsonProperty("review_count")
        Integer reviewCount,

        @JsonProperty("player_rating")
        String playerRating,

        @JsonProperty("player_review_count")
        Integer playerReviewCount,

        @JsonProperty("game_topic")
        String gameTopic,

        String product,

        @JsonProperty("rating_type")
        String ratingType,

        @JsonProperty("product_id")
        Integer productId,

        @JsonProperty("rating_type_id")
        Integer ratingTypeId,

        @JsonProperty("taken_type")
        String takenType,

        @JsonProperty("taken_team")
        Integer takenTeam,

        @JsonProperty("team_capacity")
        Integer teamCapacity,

        @JsonProperty("taken_people")
        Integer takenPeople,

        @JsonProperty("people_capacity")
        Integer peopleCapacity,  // Nullable

        @JsonProperty("max_team_players")
        Integer maxTeamPlayers,

        @JsonProperty("min_team_players")
        Integer minTeamPlayers,

        @JsonProperty("payment_type")
        String paymentType,

        String status,

        @JsonProperty("is_full")
        boolean isFull,

        @JsonProperty("is_filled")
        boolean isFilled,

        @JsonProperty("media_banner")
        MediaBanner mediaBanner,

        @JsonProperty("media_results")
        List<Object> mediaResults,  // Пустой массив, тип unknown

        @JsonProperty("slider_media")
        List<SliderMedia> sliderMedia,

        @JsonProperty("tour_count")
        Integer tourCount,  // Nullable

        Integer complexity,  // Nullable
        String duration,  // Nullable

        @JsonProperty("uploaded_media_results")
        String uploadedMediaResults,  // Nullable

        @JsonProperty("is_created_by_editor")
        boolean isCreatedByEditor,

        String currency,

        @JsonProperty("is_registration_opened")
        boolean isRegistrationOpened,

        @JsonProperty("is_privileged_registration_opened")
        boolean isPrivilegedRegistrationOpened,

        @JsonProperty("team_ids_with_privileged_registration")
        List<Integer> teamIdsWithPrivilegedRegistration,

        @JsonProperty("allowed_team_ids")
        List<Integer> allowedTeamIds,

        @JsonProperty("works_with_privileged")
        boolean worksWithPrivileged,

        @JsonProperty("works_with_allowed_teams")
        boolean worksWithAllowedTeams,

        @JsonProperty("ignores_allowed_teams_rule")
        boolean ignoresAllowedTeamsRule,

        @JsonProperty("team_ids_with_champion_registration")
        List<Integer> teamIdsWithChampionRegistration,

        @JsonProperty("works_with_champion")
        boolean worksWithChampion,

        @JsonProperty("works_with_results")
        boolean worksWithResults,

        @JsonProperty("results_filled")
        boolean resultsFilled,

        String gameId,  // Nullable
        String description,

        @JsonProperty("hasOtherUnplayedEventDays")
        boolean hasOtherUnplayedEventDays,

        @JsonProperty("hasOtherEventDays")
        boolean hasOtherEventDays,

        @JsonProperty("application_custom_design")
        ApplicationCustomDesign applicationCustomDesign,

        @JsonProperty("altern_registration_link")
        String alternRegistrationLink,  // Nullable

        @JsonProperty("partner_name")
        String partnerName,  // Nullable

        @JsonProperty("partner_social_network")
        String partnerSocialNetwork,  // Nullable

        @JsonProperty("comment_tag")
        String commentTag,  // Nullable

        @JsonProperty("link_status")
        String linkStatus,

        @JsonProperty("is_corporate_game")
        boolean isCorporateGame,

        @JsonProperty("city_link_name")
        String cityLinkName,

        @JsonProperty("works_with_multiple_registration")
        boolean worksWithMultipleRegistration
) {
}

