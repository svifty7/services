package ru.svifty7.services.domain.rudagames.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.svifty7.services.domain.rudagames.entity.AcceptedGameEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcceptedGamesRepository extends JpaRepository<AcceptedGameEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select *
            from rudagames.accepted_games ag
            where ag.uuid = :uuid
            """)
    Optional<AcceptedGameEntity> findByUuid(UUID uuid);

    @Query(nativeQuery = true, value = """
            select *
            from rudagames.accepted_games ag
            where ag.uuid in (:uuidList)
            """)
    List<AcceptedGameEntity> findByUuidList(List<UUID> uuidList);

    @Query(nativeQuery = true, value = """
            select *
            from rudagames.accepted_games ag
            where ag.event_uuid = :uuid
            limit 1
            """)
    Optional<AcceptedGameEntity> findByEventUuid(UUID uuid);

    @Query("""
            select ag
            from   AcceptedGameEntity ag
            join   ag.event e
            where  e.playAt > current_timestamp
            and    ag.notifiedAt is null
            order  by e.playAt asc
            limit  1
            """)
    Optional<AcceptedGameEntity> findClosestAcceptedAndNotNotified();
}
