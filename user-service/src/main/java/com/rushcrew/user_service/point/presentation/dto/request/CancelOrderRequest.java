package com.rushcrew.user_service.point.presentation.dto.request;

import com.rushcrew.user_service.point.application.command.CancelOrderCommand;

public record CancelOrderRequest(
    Long userId,
    String orderId,
    String sagaId
) {
    public CancelOrderCommand toCommand() {
        return new CancelOrderCommand(
            userId,
            orderId,
            sagaId
        );
    }
}
