package com.rushcrew.order_service.application.command.dto.command;

import java.util.UUID;

import lombok.Builder;

@Builder
public record CancelOrderCommand(
	UUID orderId,
	Long userId,
	String cancelReason,
	boolean isSystemCancel
) {
	// 관리자 취소 (수동)
	public static CancelOrderCommand ofAdmin(UUID orderId, Long userId, String cancelReason) {
		return new CancelOrderCommand(orderId, userId, cancelReason, false);
	}

	// 시스템 자동 취소
	public static CancelOrderCommand ofSystem(UUID orderId, Long userId, String cancelReason) {
		return new CancelOrderCommand(orderId, userId, cancelReason, true);
	}
}
