package ru.aviasales.admin.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aviasales.admin.dao.entity.SalesCategory;

@Repository
public interface SalesCategoryRepository extends JpaRepository<SalesCategory, Long> {
    boolean existsByName(String name);
}
