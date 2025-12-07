package com.rushcrew.user_service.point.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Point {

    private final Long amount;

    private Point(Long amount) {
        if (amount == null) {
            throw new IllegalArgumentException("포인트는 null 일 수 없습니다.");
        }

        if (amount < 0) {
            throw new IllegalArgumentException("포인트는 음수일 수 없습니다");
        }
        this.amount = amount;
    }

    public static Point of(long amount) {
        return new Point(amount);
    }

    public Point add(Point other) {
        return new Point(this.amount + other.getAmount());
    }

    public Point subtract(Point other) {
        if (this.amount < other.getAmount()) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다");
        }
        return new Point(this.amount - other.getAmount());
    }

    public boolean isGreaterThanOrEqual(Point other) {
        return this.amount >= other.getAmount();
    }
}
