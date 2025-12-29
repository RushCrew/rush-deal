package com.rushcrew.user_service.point.application.command;

public record CreatePendingPointCommand(
    Long userId,
    String orderId,
    Long amount,
    String sagaId
) {}
