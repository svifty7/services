package ru.svifty7.services.domain.rudagames.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.svifty7.services.domain.rudagames.entity.TeamEntity;

@Repository
public interface TeamsRepository extends JpaRepository<TeamEntity, Integer> {
}
