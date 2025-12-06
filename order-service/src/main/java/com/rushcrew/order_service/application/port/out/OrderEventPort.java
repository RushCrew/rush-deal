package com.rushcrew.order_service.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface OrderEventPort {

	// 재고 소진 이벤트 발행
	void publishStockDepletedEvent(
		UUID timeDealId,
		Long userId,
		Integer availableStock,
		Instant timestamp
	);
}
