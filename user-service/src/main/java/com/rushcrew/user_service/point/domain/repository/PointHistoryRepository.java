package com.rushcrew.user_service.point.domain.repository;

import com.rushcrew.user_service.point.domain.entity.PointHistory;
import java.util.List;

public interface PointHistoryRepository {
    void save(PointHistory pointHistory);
    void saveAll(List<PointHistory> pointHistories);
}
