package com.rushcrew.user_service.point.infrastructure.messaging.dto;

public record PointRefundRequestedEvent(
    Long userId,
    String orderId,
    Long finalAmount,
    String sagaId
) {}
