package com.rushcrew.order_service.presentation.dto.response;

import java.util.UUID;

import com.rushcrew.order_service.application.command.dto.result.RefundOrderResult;

public record RefundOrderResponse(
	UUID orderId,
	String message
) {
	public static RefundOrderResponse from(RefundOrderResult result) {
		return new RefundOrderResponse(result.orderId(), result.message());
	}
}
