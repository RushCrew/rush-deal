package com.rushcrew.user_service.point.domain.vo;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.user_service.point.exception.PointErrorCode;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderId {

    private String id;

    private OrderId(String orderId) {
        if (!StringUtils.hasText(orderId)) {
        throw new BusinessException(PointErrorCode.INVALID_ORDER_ID);
    }
        this.id = orderId;
    }

    public static OrderId of(String id) {
        return new OrderId(id);
    }
}
