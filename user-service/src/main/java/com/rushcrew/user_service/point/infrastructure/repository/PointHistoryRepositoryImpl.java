package com.rushcrew.user_service.point.infrastructure.repository;

import com.rushcrew.user_service.point.domain.entity.PointHistory;
import com.rushcrew.user_service.point.domain.repository.PointHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public void save(PointHistory pointHistory) {
        pointHistoryJpaRepository.save(pointHistory);
    }

    @Override
    public void saveAll(List<PointHistory> pointHistories) {
        pointHistoryJpaRepository.saveAll(pointHistories);
    }
}
