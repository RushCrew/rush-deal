package com.rushcrew.order_service.infrastructure.dto.point;

import lombok.Builder;

@Builder
public record UsePointRequest(
	Long userId,
	String orderId,
	Long amount,
	String sagaId
) {}
