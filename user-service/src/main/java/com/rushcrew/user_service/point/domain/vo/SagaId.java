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
public class SagaId {

    private String id;

    private SagaId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException(PointErrorCode.INVALID_ORDER_ID);
        }
        this.id = id;

    }

    public static SagaId of(String id) {
        return new SagaId(id);
    }
}
