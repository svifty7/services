package ru.svifty7.services.domain.rudagames.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.svifty7.services.domain.rudagames.entity.EventEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventsRepository extends JpaRepository<EventEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select *
            from rudagames.events e
            where e.uuid = :uuid
            """)
    Optional<EventEntity> findByUuid(UUID uuid);

    @Query(nativeQuery = true, value = """
            select *
            from rudagames.events e
            where e.uuid in (:uuidList)
            """)
    List<EventEntity> findByUuidList(List<UUID> uuidList);

    @Query(value = """
            select e.*
            from rudagames.events e
            left join rudagames.accepted_games ag on ag.event_uuid = e.uuid
            where e.play_at > current_timestamp
            and ag.uuid is null
            and e.announced_at is null
            order by e.registration_start_at
            limit 1
            """, nativeQuery = true)
    Optional<EventEntity> findLastNotAcceptedAndIsNotAnnounced();
}
