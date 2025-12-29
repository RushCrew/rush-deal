package com.rushcrew.order_service.application.port.out;

import java.util.UUID;

public interface QueueEventPort {
	void publishTokenRemoveEvent(Long userId, UUID productId, String token);
}
