package ru.aviasales.admin.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aviasales.admin.dao.entity.AdType;

@Repository
public interface AdTypeRepository extends JpaRepository<AdType, Long> {
}
