package ru.aviasales.admin.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aviasales.common.dao.entity.SalesCategory;
import ru.aviasales.common.dao.entity.SalesUnit;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesUnitRepository extends JpaRepository<SalesUnit, Long> {
    boolean existsByName(String name);

    Page<SalesUnit> findAllByCategory(SalesCategory category, Pageable pageable);
}
