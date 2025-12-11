package com.rushcrew.user_service.point.presentation.dto.request;

import com.rushcrew.user_service.point.application.command.CancelOrderCommand;
import java.util.UUID;

public record CancelOrderRequest(
    Long userId,
    UUID orderId
) {
    public CancelOrderCommand toCommand() {
        return new CancelOrderCommand(
            userId,
            orderId
        );
    }
}
