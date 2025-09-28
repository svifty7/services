package ru.svifty7.services.domain.rudagames.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface EventsMapper {
    @Mapping(target = "uuid", source = "eventRecordId")
    @Mapping(target = "name", source = "gameType")
    @Mapping(target = "playAt", source = "playedAt")
    @Mapping(target = "registrationStartAt", source = "registrationAt")
    @Mapping(target = "maxPlayersInTeam", source = "maxTeamPlayers")
    @Mapping(target = "minPlayersInTeam", source = "minTeamPlayers")
    @Mapping(target = "tag", source = "commentTag")
    @Mapping(target = "imageUrl", source = "mediaBanner.head")
    @Mapping(target = "maxTeamCount", source = "teamCapacity")
    @Mapping(target = "currentTeamCount", source = "takenTeam")
    EventEntity toEntity(CityEvent event);
}
