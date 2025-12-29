package com.rushcrew.order_service.infrastructure.dto.point;

import lombok.Builder;

@Builder
public record PointBalanceResponse(
	Long userId,
	Long balance
) {}
