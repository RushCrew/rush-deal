package com.rushcrew.user_service.point.domain.vo;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.user_service.point.exception.PointErrorCode;
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
            throw new BusinessException(PointErrorCode.INVALID_ORDER_ID);
        }
        this.id = orderId;
    }

    public static OrderId of(UUID id) {
        return new OrderId(id);
    }
}
