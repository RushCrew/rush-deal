package com.rushcrew.user_service.point.infrastructure.repository;

import com.rushcrew.user_service.point.domain.entity.PointHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, UUID> {
}
