package com.rushcrew.user_service.point.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    USE("사용"),
    EARN("적립"),
    REFUND("환불"),
    EXPIRE("만료");

    private final String description;
}
