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
public class UserId {

    private Long id;

    private UserId(Long orderId) {
        if (orderId == null) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);
        }
        this.id = orderId;
    }

    public static UserId of(Long id) {
        return new UserId(id);
    }
}
