package ru.svifty7.services.domain.rudagames.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "rudagames", name = "accepted_games")
public class AcceptedGameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @JoinColumn(name = "event_uuid", referencedColumnName = "uuid")
    @OneToOne(fetch = FetchType.EAGER, targetEntity = EventEntity.class)
    private EventEntity event;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @JoinColumn(name = "team_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = TeamEntity.class)
    private TeamEntity team;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "players_count")
    private Integer playersCount;

    public boolean getIsAccepted() {
       return acceptedAt != null;
    }

    public boolean getIsNotified() {
       return notifiedAt != null;
    }

}
