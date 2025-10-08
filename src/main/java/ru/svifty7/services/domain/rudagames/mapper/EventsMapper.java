package ru.svifty7.services.domain.rudagames.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;
import ru.svifty7.services.domain.rudagames.entity.ProductEntity;
import ru.svifty7.services.domain.rudagames.repository.ProductsRepository;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public abstract class EventsMapper {

    @Autowired
    private ProductsRepository productsRepository;

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
    @Mapping(target = "product", source = "productId")
    public abstract EventEntity toEntity(CityEvent event);

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
    @Mapping(target = "product", source = "productId")
    public abstract void updateEntity(CityEvent event, @MappingTarget EventEntity target);

    public ProductEntity map(Integer productId) {
        if (productId == null) {
            return null;
        }
        return productsRepository.findById(productId).orElse(null);
    }
}
