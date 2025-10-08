package ru.svifty7.services.domain.rudagames.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products", schema = "rudagames")
public class ProductEntity {

    @Id
    @Column(updatable = false, nullable = false, unique = true)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String link;
}
