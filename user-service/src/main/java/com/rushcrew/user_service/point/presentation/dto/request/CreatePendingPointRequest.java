package com.rushcrew.user_service.point.presentation.dto.request;

import com.rushcrew.user_service.point.application.command.CreatePendingPointCommand;
import java.util.UUID;

public record CreatePendingPointRequest(
    Long userId,
    UUID orderId,
    Long amount
) {
    public CreatePendingPointCommand toCommand() {
        return new CreatePendingPointCommand(
            userId,
            orderId,
            amount
        );
    }
}
