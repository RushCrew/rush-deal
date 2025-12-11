package com.rushcrew.user_service.point.presentation.dto.request;

import com.rushcrew.user_service.point.application.command.UsePointCommand;
import java.util.UUID;

public record UsePointRequest(
    Long userId,
    UUID orderId,
    Long amount
) {
    public UsePointCommand toCommand() {
        return new UsePointCommand(
            userId,
            orderId,
            amount
            );
    }
}
