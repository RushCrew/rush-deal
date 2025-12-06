package com.rushcrew.order_service.application.command.port.out;

import java.util.Optional;
import java.util.UUID;

import com.rushcrew.order_service.domain.model.order.Order;

/**
 * 주문 Command Port (쓰기 전용)
 */
public interface OrderCommandPort {

	Order save(Order order);

	Optional<Order> findById(UUID orderId);

}
