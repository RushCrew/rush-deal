package com.rushcrew.order_service.application.command.dto.command;

import java.util.UUID;

import lombok.Builder;

@Builder
public record RefundOrderCommand(
	UUID orderId,
	Long userId,
	String reason
) {
	public static RefundOrderCommand of(UUID orderId, Long userId, String reason) {
		return new RefundOrderCommand(orderId, userId, reason);
	}
}
