package ru.aviasales.admin.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aviasales.admin.dao.entity.SalesUnit;

@Repository
public interface SalesUnitRepository extends JpaRepository<SalesUnit, Long> {
}
