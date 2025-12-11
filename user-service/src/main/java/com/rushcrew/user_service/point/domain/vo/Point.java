package com.rushcrew.user_service.point.domain.vo;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.user_service.point.exception.PointErrorCode;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

    private Long amount;

    private Point(Long amount) {
        if (amount == null) {
            throw new BusinessException(PointErrorCode.INVALID_POINT_AMOUNT);
        }

        if (amount < 0) {
            throw new BusinessException(PointErrorCode.INVALID_POINT_AMOUNT);
        }
        this.amount = amount;
    }

    public static Point of(long amount) {
        return new Point(amount);
    }

    public Point add(Long amount) {
        return new Point(this.amount + amount);
    }

    public static Point zero() {
        return new Point(0L);
    }

    public Point subtract(Long amount) {
        if (this.amount < amount) {
            throw new BusinessException(PointErrorCode.INSUFFICIENT_BALANCE);
        }
        return new Point(this.amount - amount);
    }
}
