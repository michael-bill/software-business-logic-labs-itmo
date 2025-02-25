package ru.aviasales.admin.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aviasales.admin.dao.entity.UserSegment;

@Repository
public interface UserSegmentRepository extends JpaRepository<UserSegment, Long> {
}
