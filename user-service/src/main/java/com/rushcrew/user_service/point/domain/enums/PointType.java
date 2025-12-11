package com.rushcrew.user_service.point.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    EARN_PENDING("적립대기") {
        @Override
        public boolean isEarnCancellable() { return true; }
    },
    EARN_CONFIRM("레거시확정"),
    EARN("적립확정"),
    USE_PENDING("사용대기") {
        @Override
        public boolean isUseCancellable() { return true; }
    },
    USE_CONFIRM("사용확정"),
    EARN_CANCEL("적립취소"),

    USE_CANCEL("사용취소");

    private final String description;

    public boolean isEarnCancellable() { return false; }
    public boolean isUseCancellable() { return false; }

    public boolean isCanceledStatus() {
        return this == EARN_CANCEL || this == USE_CANCEL;
    }

    public boolean isConfirmedStatus() {
        return this == EARN_CONFIRM; // 필요하다면 USE_CONFIRM 등 추가
    }
}
