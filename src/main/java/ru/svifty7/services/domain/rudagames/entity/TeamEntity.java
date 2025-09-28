package ru.svifty7.services.domain.rudagames.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "teams", schema = "rudagames")
public class TeamEntity {
    @Id
    @Column(updatable = false, nullable = false, unique = true)
    private Integer id;

    @Column(columnDefinition = "varchar(32)", nullable = false)
    private String name;
}
