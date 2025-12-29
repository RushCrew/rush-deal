package com.rushcrew.order_service.application.port.out;

public interface MetricsPort {

	// ==================== Saga 메트릭 ====================

	void recordSagaSuccess();
	void recordSagaFailure();
	void recordSagaTimeout();
	void recordSagaRecoveryFailure();

	// ==================== Outbox 메트릭 ====================

	void recordOutboxPublished();

	// ==================== Redis 캐시 메트릭 ====================

	void recordCacheHit();
	void recordCacheMiss();

	// ==================== 주문 생성 타이머 ====================

	Object startOrderCreationTimer();
	void stopOrderCreationTimer(Object sample);
}
