package com.rushcrew.user_service.point.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointWalletId {

    private Long id;

    private PointWalletId(final Long id) {
        this.id = id;
    }

    public static PointWalletId of(Long id) {
        return new PointWalletId(id);
    }
}
