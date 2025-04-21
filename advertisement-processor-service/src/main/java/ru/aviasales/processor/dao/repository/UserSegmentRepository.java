package ru.aviasales.processor.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aviasales.common.dao.entity.UserSegment;

@Repository
public interface UserSegmentRepository extends JpaRepository<UserSegment, Long> {
}
