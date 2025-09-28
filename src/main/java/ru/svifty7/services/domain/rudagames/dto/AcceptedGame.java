package ru.svifty7.services.domain.rudagames.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.svifty7.services.domain.rudagames.dto.serializer.MskInstantDeserializer;

import java.time.Instant;
import java.util.UUID;

public record AcceptedGame(

        @JsonProperty("id")
        Integer id,

        @JsonProperty("games_event_day_id")
        UUID gamesEventDayId,

        @JsonProperty("game_name")
        String gameName,

        @JsonProperty("event_record_id")
        UUID eventRecordId,

        @JsonProperty("played_at")
        @JsonDeserialize(using = MskInstantDeserializer.class)
        Instant playedAt,

        @JsonProperty("place")
        String place,

        @JsonProperty("address")
        String address,

        @JsonProperty("type")
        String type,

        @JsonProperty("game_type")
        String gameType,

        @JsonProperty("displayed_parent_name")
        String displayedParentName,

        @JsonProperty("game_topic")
        String gameTopic,

        @JsonProperty("distribution_format")
        String distributionFormat,

        @JsonProperty("time")
        String time,

        @JsonProperty("status")
        String status,

        @JsonProperty("player_count")
        Integer playerCount,

        @JsonProperty("total_cost")
        Integer totalCost,

        @JsonProperty("paid_total")
        Integer paidTotal,

        @JsonProperty("actual_player_count")
        Integer actualPlayerCount,

        @JsonProperty("actual_free_player_count")
        Integer actualFreePlayerCount,

        @JsonProperty("actual_payable_player_count")
        Integer actualPayablePlayerCount,

        @JsonProperty("discount")
        Integer discount,

        @JsonProperty("comment")
        String comment,

        @JsonProperty("play_for_first_time")
        Boolean playForFirstTime,

        @JsonProperty("is_actually_new")
        Boolean isActuallyNew,

        @JsonProperty("team_id")
        Integer teamId,

        @JsonProperty("created_at")
        @JsonDeserialize(using = MskInstantDeserializer.class)
        Instant createdAt,

        @JsonProperty("updated_at")
        @JsonDeserialize(using = MskInstantDeserializer.class)
        Instant updatedAt,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("live_participant_id")
        Integer liveParticipantId,

        @JsonProperty("promocode")
        String promocode,

        @JsonProperty("promocode_discount")
        Integer promocodeDiscount,

        @JsonProperty("max_team_players")
        Integer maxTeamPlayers,

        @JsonProperty("min_team_players")
        Integer minTeamPlayers

) {}
