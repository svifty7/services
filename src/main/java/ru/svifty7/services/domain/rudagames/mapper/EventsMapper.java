package ru.svifty7.services.domain.rudagames.mapper;

import org.mapstruct.*;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;
import ru.svifty7.services.domain.rudagames.entity.ProductEntity;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface EventsMapper {

    @Mapping(target = "uuid", source = "cityEvent.eventRecordId")
    @Mapping(target = "name", source = "cityEvent.gameType")
    @Mapping(target = "playAt", source = "cityEvent.playedAt")
    @Mapping(target = "registrationStartAt", source = "cityEvent.registrationAt")
    @Mapping(target = "maxPlayersInTeam", source = "cityEvent.maxTeamPlayers")
    @Mapping(target = "minPlayersInTeam", source = "cityEvent.minTeamPlayers")
    @Mapping(target = "tag", source = "cityEvent.commentTag")
    @Mapping(target = "imageUrl", source = "cityEvent.mediaBanner.head")
    @Mapping(target = "maxTeamCount", source = "cityEvent.teamCapacity")
    @Mapping(target = "currentTeamCount", source = "cityEvent.takenTeam")
    @Mapping(target = "product", source = "cityEvent")
    EventEntity toEntity(
            CityEvent cityEvent,
            @Context Map<Integer, ProductEntity> productsMap
    );

    @Mapping(target = "uuid", source = "cityEvent.eventRecordId")
    @Mapping(target = "name", source = "cityEvent.gameType")
    @Mapping(target = "playAt", source = "cityEvent.playedAt")
    @Mapping(target = "registrationStartAt", source = "cityEvent.registrationAt")
    @Mapping(target = "maxPlayersInTeam", source = "cityEvent.maxTeamPlayers")
    @Mapping(target = "minPlayersInTeam", source = "cityEvent.minTeamPlayers")
    @Mapping(target = "tag", source = "cityEvent.commentTag")
    @Mapping(target = "imageUrl", source = "cityEvent.mediaBanner.head")
    @Mapping(target = "maxTeamCount", source = "cityEvent.teamCapacity")
    @Mapping(target = "currentTeamCount", source = "cityEvent.takenTeam")
    @Mapping(target = "product", source = "cityEvent")
    EventEntity toUpdate(
            CityEvent cityEvent,
            @MappingTarget EventEntity target,
            @Context Map<Integer, ProductEntity> productsMap
    );

    default ProductEntity map(
            CityEvent cityEvent,
            @Context Map<Integer, ProductEntity> productsMap
    ) {
        if (cityEvent.productId() == null) {
            return null;
        }
        return productsMap.get(cityEvent.productId());
    }
}
