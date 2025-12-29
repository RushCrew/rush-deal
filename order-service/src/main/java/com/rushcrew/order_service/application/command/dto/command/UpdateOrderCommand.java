package com.rushcrew.order_service.application.command.dto.command;

import java.util.UUID;

import com.rushcrew.order_service.domain.vo.ShippingInfo;

import lombok.Builder;

@Builder
public record UpdateOrderCommand(
	UUID orderId,
	Long userId,
	ShippingInfo shippingInfo,  // null 가능 (수정하지 않을 경우)
	Long pointUsed  // null 가능 (수정하지 않을 경우)
) {}
