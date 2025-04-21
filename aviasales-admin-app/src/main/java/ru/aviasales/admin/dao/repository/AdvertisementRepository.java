package ru.aviasales.admin.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aviasales.common.dao.entity.Advertisement;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
}
