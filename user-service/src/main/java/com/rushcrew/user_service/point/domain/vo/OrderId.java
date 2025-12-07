package com.rushcrew.user_service.point.domain.vo;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderId {

    private UUID id;

    private OrderId(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("유효하지 않은 주문 ID 입니다.");
        }
        this.id = orderId;
    }

    public static OrderId of(UUID id) {
        return new OrderId(id);
    }
}
