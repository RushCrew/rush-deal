package com.rushcrew.order_service.application.query.usecase;

import java.util.UUID;

import com.rushcrew.order_service.application.query.dto.OrderDetailDto;

public interface GetOrderDetailUseCase {
	OrderDetailDto getOrderDetail(UUID orderId, Long userId, String role);
}
