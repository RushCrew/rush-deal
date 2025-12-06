package com.rushcrew.order_service.application.command.port.out;

import com.rushcrew.order_service.application.command.dto.result.CreateOrderResult;

public interface OrderCachePort {

	/**
	 * 주문 캐시 저장 (주문 생성 시)
	 */
	void saveOrderCache(CreateOrderResult result);

}
