package com.rushcrew.user_service.point.infrastructure.repository;

import static com.rushcrew.user_service.point.domain.entity.QPointHistory.pointHistory;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rushcrew.user_service.point.domain.entity.PointHistory;
import com.rushcrew.user_service.point.domain.enums.PointType;
import com.rushcrew.user_service.point.domain.repository.PointHistoryQueryRepository;
import com.rushcrew.user_service.point.domain.vo.OrderId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointHistoryQueryRepositoryImpl
    implements PointHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsEarnedHistoryByOrderId(UUID orderId) {
        return (
            queryFactory
                .selectOne()
                .from(pointHistory)
                .where(
                    pointHistory.orderId.eq(OrderId.of(orderId)),
                    pointHistory.type.eq(PointType.EARN_CONFIRM)
                )
                .fetchFirst() != null
        );
    }

    @Override
    public List<PointHistory> findAllByOrderId(UUID orderId) {
        return queryFactory
            .selectFrom(pointHistory)
            .where(pointHistory.orderId.eq(OrderId.of(orderId)))
            .orderBy(pointHistory.createdAt.asc())
            .fetch();
    }

    @Override
    public boolean existsHistoryByOrderId(UUID orderId) {
        return (
            queryFactory
                .selectOne()
                .from(pointHistory)
                .where(pointHistory.orderId.eq(OrderId.of(orderId)))
                .fetchFirst() != null
        );
    }

    @Override
    public List<PointHistory> findPendingHistories(
        PointType type,
        LocalDateTime threshold,
        int limit
    ) {
        Long anyUserId = queryFactory
            .select(pointHistory.userId.id)
            .from(pointHistory)
            .where(
                pointHistory.type.eq(type),
                pointHistory.createdAt.loe(threshold),
                pointHistory.confirmedAt.isNull()
            )
            .limit(1)
            .fetchOne();

        if (anyUserId == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(pointHistory)
            .where(
                pointHistory.type.eq(type),
                pointHistory.userId.id.eq(anyUserId),
                pointHistory.createdAt.loe(threshold),
                pointHistory.confirmedAt.isNull()
            )
            .orderBy(pointHistory.createdAt.asc())
            .limit(limit)
            .fetch();
    }



    @Override
    public Optional<PointHistory> findLatestByUserId(Long userId) {

        PointHistory result = queryFactory
            .selectFrom(pointHistory)
            .where(pointHistory.userId.id.eq(userId))
            .orderBy(
                Expressions.dateTimeTemplate(
                    LocalDateTime.class,
                    "GREATEST({0}, {1})",
                    pointHistory.confirmedAt,
                    pointHistory.createdAt
                ).desc()
            )
            .fetchFirst();

        return Optional.ofNullable(result);
    }


    @Override
    public void bulkUpdateToConfirmed(List<UUID> ids, LocalDateTime confirmTime) {
        if (ids.isEmpty()) return;

        queryFactory
            .update(pointHistory)
            .set(pointHistory.confirmedAt, confirmTime)
            .where(pointHistory.id.in(ids))
            .execute();
    }
}
