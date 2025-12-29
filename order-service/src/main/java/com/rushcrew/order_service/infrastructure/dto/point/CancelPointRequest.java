package com.rushcrew.order_service.infrastructure.dto.point;

import lombok.Builder;

@Builder
public record CancelPointRequest(
	Long userId,
	String orderId,
	String sagaId
) {}
