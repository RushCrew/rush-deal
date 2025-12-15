package com.rushcrew.user_service.point.application.command;

public record CancelOrderCommand(
    Long userId,
    String orderId,
    String sagaId
) {}
