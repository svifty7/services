package ru.svifty7.services.domain.rudagames.mapper;

import org.mapstruct.*;
import ru.svifty7.services.domain.rudagames.dto.AcceptedGame;
import ru.svifty7.services.domain.rudagames.entity.AcceptedGameEntity;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;
import ru.svifty7.services.domain.rudagames.entity.TeamEntity;

import java.util.Map;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface AcceptedGamesMapper {

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "notifiedAt", ignore = true)
    @Mapping(target = "messageId", ignore = true)
    @Mapping(target = "playersCount", ignore = true)
    @Mapping(target = "acceptedAt", source = "createdAt")
    @Mapping(target = "team", expression = "java(mapTeam(acceptedGame, teamsMap))")
    @Mapping(target = "event", expression = "java(mapEvent(acceptedGame, eventsMap))")
    AcceptedGameEntity toEntity(
            AcceptedGame acceptedGame,
            @Context Map<UUID, EventEntity> eventsMap,
            @Context Map<Integer, TeamEntity> teamsMap
    );

    @Mapping(target = "acceptedAt", source = "createdAt")
    @Mapping(target = "team", expression = "java(mapTeam(acceptedGame, teamsMap))")
    @Mapping(target = "event", expression = "java(mapEvent(acceptedGame, eventsMap))")
    AcceptedGameEntity toUpdate(
            AcceptedGame acceptedGame,
            @MappingTarget AcceptedGameEntity target,
            @Context Map<UUID, EventEntity> eventsMap,
            @Context Map<Integer, TeamEntity> teamsMap
    );

    default EventEntity mapEvent(
            AcceptedGame acceptedGame,
            Map<UUID, EventEntity> eventsMap
    ) {
        if (acceptedGame.eventRecordId() == null) {
            return null;
        }
        return eventsMap.get(acceptedGame.eventRecordId());
    }

    default TeamEntity mapTeam(
            AcceptedGame acceptedGame,
            Map<Integer, TeamEntity> teamsMap
    ) {
        if (acceptedGame.teamId() == null) {
            return null;
        }
        return teamsMap.get(acceptedGame.teamId());
    }
}
