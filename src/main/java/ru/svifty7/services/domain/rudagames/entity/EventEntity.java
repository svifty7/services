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
@Table(schema = "rudagames", name = "events")
public class EventEntity {

    @Id
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(name = "play_at", nullable = false)
    private Instant playAt;

    @Column(name = "registration_start_at", nullable = false)
    private Instant registrationStartAt;

    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = ProductEntity.class)
    private ProductEntity product;

    private String description;

    @Column(name = "tour_count")
    private Integer tourCount;

    @Column(name = "max_players_in_team")
    private Integer maxPlayersInTeam;

    @Column(name = "min_players_in_team")
    private Integer minPlayersInTeam;

    @Column(name = "is_registration_opened")
    private Boolean isRegistrationOpened;

    @Column(name = "is_full")
    private Boolean isFull;

    private String tag;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "max_team_count")
    private Integer maxTeamCount;

    @Column(name = "current_team_count")
    private Integer currentTeamCount;

    private String type;

    @Column(name = "announced_at")
    private Instant announcedAt;

    private boolean getIsAnnounced() {
        return announcedAt != null;
    }

}
