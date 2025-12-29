package com.rushcrew.user_service.point.presentation.dto.request;

import com.rushcrew.user_service.point.application.command.CreatePendingPointCommand;

public record CreatePendingPointRequest(
    Long userId,
    String orderId,
    Long amount,
    String sagaId
) {
    public CreatePendingPointCommand toCommand() {
        return new CreatePendingPointCommand(
            userId,
            orderId,
            amount,
            sagaId
        );
    }
}
