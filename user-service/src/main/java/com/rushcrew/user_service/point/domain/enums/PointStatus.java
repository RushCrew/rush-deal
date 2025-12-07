package com.rushcrew.user_service.point.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointStatus {
    PENDING("대기") {
        @Override
        public boolean canBeCancelled(PointStatus newStatus) {
            return true;
        }
    },
    COMPLETED("완료") {
        @Override
        public boolean canBeCancelled(PointStatus newStatus) {
            return false;
        }
    },
    CANCELED("취소됨") {
        @Override
        public boolean canBeCancelled(PointStatus newStatus) {
            return false;
        }
    },
    EXPIRED("만료됨") {
        @Override
        public boolean canBeCancelled(PointStatus newStatus) {
            return false;
        }
    };

    private final String description;

    public abstract boolean canBeCancelled(PointStatus newStatus);
}


