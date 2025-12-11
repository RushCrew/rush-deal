package com.rushcrew.user_service.point.domain.service;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.user_service.point.domain.entity.PointHistory;
import com.rushcrew.user_service.point.domain.repository.PointHistoryQueryRepository;
import com.rushcrew.user_service.point.domain.vo.OrderId;
import com.rushcrew.user_service.point.domain.vo.Point;
import com.rushcrew.user_service.point.domain.vo.UserId;
import com.rushcrew.user_service.point.exception.PointErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointDomainService {

    private final PointHistoryQueryRepository queryRepository;

    public PointDomainService(PointHistoryQueryRepository pointHistoryQueryRepository) {
        this.queryRepository = pointHistoryQueryRepository;
    }

    // 적립 대기 포인트 생성
    public PointHistory createPendingEarnHistory(
        UserId userId,
        OrderId orderId,
        Point amount
    ) {
        // 1. 중복 적립 검증
        validateNotAlreadyEarned(orderId);

        // 2. 현재 잔액 조회
        Point currentBalance = getCurrentBalance(userId);

        // 3. 적립 대기 이력 생성
        return PointHistory.createPendingEarn(
            userId,
            orderId,
            amount,
            currentBalance
        );
    }

    // 포인트 사용 대기 이력 생성
    public PointHistory createPendingUseHistory(
        UserId userId,
        OrderId orderId,
        Point amount)
    {
        // 1. 중복 사용 검증
        validateNotAlreadyUsed(orderId);

        // 2. 현재 잔액 조회
        Point currentBalance = getCurrentBalance(userId);


        // 3. 사용 이력 생성
        return PointHistory.createPendingUse(
            userId,
            orderId,
            amount,
            currentBalance
        );
    }


    // 주문 취소 시 해당 주문의 모든 예비 포인트 취소 이력 생성
    public List<PointHistory> cancelHistoriesForOrder(UserId userId, OrderId orderId) {
        // 1. 관련 이력 일괄 조회
        List<PointHistory> pointHistories = queryRepository.findAllByOrderId(orderId.getId());

        // 2. 도메인 유효성 검증
        validateCancelable(pointHistories, userId);

        // 3. 현재 잔액 조회 (계산의 기준점)
        Point currentBalance = getCurrentBalance(userId);

        // 4. 취소 이력 생성 및 잔액 최신화 (Business Logic)
        return createCancelHistories(pointHistories, currentBalance);
    }



    // 중복 사용 검증
    private void validateNotAlreadyUsed(OrderId orderId) {
        if (queryRepository.existsHistoryByOrderId(orderId.getId())) {
            throw new BusinessException(PointErrorCode.DUPLICATE_ORDER_PROCESSING);
        }
    }



    // 중복 적립 검증
    private void validateNotAlreadyEarned(OrderId orderId) {
        boolean alreadyEarned = queryRepository.existsEarnedHistoryByOrderId(orderId.getId());

        if (alreadyEarned) {
            throw new BusinessException(PointErrorCode.DUPLICATE_POINT_EARN);
        }
    }

    // 현재 잔액 조회
    private Point getCurrentBalance(UserId userId) {
        return queryRepository
            .findLatestByUserId(userId.getId())
            .map(PointHistory::getBalanceAfter)
            .orElse(Point.zero());
    }

    // 취소 가능 여부 검증
    private void validateCancelable(List<PointHistory> histories, UserId userId) {
        if (histories.isEmpty()) {
            throw new BusinessException(PointErrorCode.POINT_HISTORY_NOT_FOUND);
        }

        // 소유권 검증
        if (histories.stream().anyMatch(history -> !history.isOwnedBy(userId))) {
            throw new BusinessException(PointErrorCode.UNAUTHORIZED_POINT_ACCESS);
        }

        // 이미 확정된 적립인지 검증
        if (histories.stream().anyMatch(PointHistory::isEarnConfirmed)) {
            throw new BusinessException(PointErrorCode.POINT_ALREADY_CONFIRMED);
        }

        // 이미 취소된 주문인지 검증
        if (histories.stream().anyMatch(PointHistory::isCanceled)) {
            throw new BusinessException(PointErrorCode.POINT_ALREADY_CANCELED);
        }
    }

    // 취소 이력 리스트 생성
    private List<PointHistory> createCancelHistories(List<PointHistory> pointHistories, Point initialBalance) {
        List<PointHistory> newHistories = new ArrayList<>();
        List<PointHistory> updatedHistories = new ArrayList<>();
        Point updatedBalance = initialBalance;

        for (PointHistory history : pointHistories) {
            Optional<PointHistory> cancelHistory = history.cancelIfPossible(updatedBalance);

            if (cancelHistory.isPresent()) {
                updatedHistories.add(history);
                newHistories.add(cancelHistory.get());
                updatedBalance = cancelHistory.get().getBalanceAfter();
            }
        }
        newHistories.addAll(updatedHistories);
        return newHistories;
    }
}
