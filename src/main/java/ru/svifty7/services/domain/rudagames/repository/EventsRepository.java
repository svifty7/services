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
        select *
        from   rudagames.events e
        where  e.accepted_at is not null
          and  e.notified_at is null
          and  e.play_at > current_timestamp
        order  by e.play_at
        limit  1
        """, nativeQuery = true)
    Optional<EventEntity> findLastAcceptedAndIsNotNotified();

    @Query(value = """
        select *
        from   rudagames.events e
        where  e.announced_at is null
          and  e.accepted_at  is null
          and  e.play_at      > current_timestamp
        order  by e.registration_start_at
        limit  1
        """, nativeQuery = true)
    Optional<EventEntity> findLastNotAcceptedAndIsNotAnnounced();

}
