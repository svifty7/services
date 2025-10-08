package ru.svifty7.services.domain.rudagames.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.svifty7.services.domain.rudagames.entity.ProductEntity;

@Repository
public interface ProductsRepository extends JpaRepository<ProductEntity, Integer> {
}
