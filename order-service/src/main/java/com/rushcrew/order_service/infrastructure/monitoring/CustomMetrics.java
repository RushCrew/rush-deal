package com.rushcrew.order_service.infrastructure.monitoring;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.port.out.MetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomMetrics implements MetricsPort {

	private final MeterRegistry meterRegistry;

	// ==================== Saga 메트릭 ====================

	// Saga 성공/실패 카운터
	@Override
	public void recordSagaSuccess() {
		Counter.builder("saga.execution.success")
			.tag("type", "order_creation")
			.description("성공적으로 완료된 Saga 수")
			.register(meterRegistry)
			.increment();
	}

	@Override
	public void recordSagaFailure() {
		Counter.builder("saga.execution.failure")
			.tag("type", "order_creation")
			.description("실패한 Saga 수 (비즈니스 로직 실패)")
			.register(meterRegistry)
			.increment();
	}

	// Saga 타임아웃 메트릭
	@Override
	public void recordSagaTimeout() {
		Counter.builder("saga.execution.timeout")
			.tag("type", "order_creation")
			.tag("reason", "stock_reservation_timeout")
			.description("10분 이상 응답 없어 타임아웃 처리된 Saga 수")
			.register(meterRegistry)
			.increment();
	}

	// Saga 복구 실패 메트릭
	@Override
	public void recordSagaRecoveryFailure() {
		Counter.builder("saga.recovery.failure")
			.tag("type", "order_creation")
			.description("타임아웃 Saga 복구(보상) 실패 수")
			.register(meterRegistry)
			.increment();
	}

	// ==================== Outbox 메트릭 ====================

	// Outbox 이벤트 발행 메트릭
	@Override
	public void recordOutboxPublished() {
		Counter.builder("outbox.event.published")
			.description("Outbox 이벤트 발행 성공 수")
			.register(meterRegistry)
			.increment();
	}

	// ==================== Redis 캐시 메트릭 ====================

	// Redis 캐시 히트/미스
	@Override
	public void recordCacheHit() {
		Counter.builder("redis.cache.hit")
			.description("Redis 캐시 히트 수")
			.register(meterRegistry)
			.increment();
	}

	@Override
	public void recordCacheMiss() {
		Counter.builder("redis.cache.miss")
			.description("Redis 캐시 미스 수")
			.register(meterRegistry)
			.increment();
	}

	// ==================== 주문 생성 타이머 ====================

	// 주문 생성 소요 시간
	@Override
	public Object startOrderCreationTimer() {
		return Timer.start(meterRegistry);
	}

	@Override
	public void stopOrderCreationTimer(Object sample) {
		if (sample instanceof Timer.Sample timerSample) {
			timerSample.stop(Timer.builder("order.creation.duration")
				.description("주문 생성 API 처리 소요 시간")
				.register(meterRegistry));
		}
	}

	// ==================== 캐시 동기화 메트릭 ====================

	/**
	 * 캐시 동기화 성공
	 */
	public void recordCacheSyncSuccess(String eventType, long durationMs) {
		Counter.builder("cache.sync.success")
			.tag("event_type", eventType)
			.tag("tier", "event_consumer")
			.description("이벤트 기반 캐시 동기화 성공 수")
			.register(meterRegistry)
			.increment();

		Timer.builder("cache.sync.duration")
			.tag("event_type", eventType)
			.tag("tier", "event_consumer")
			.description("캐시 동기화 소요 시간")
			.register(meterRegistry)
			.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	/**
	 * 캐시 동기화 실패
	 */
	public void recordCacheSyncFailure(String eventType, String errorType) {
		Counter.builder("cache.sync.failure")
			.tag("event_type", eventType)
			.tag("error_type", errorType)
			.tag("tier", "event_consumer")
			.description("이벤트 기반 캐시 동기화 실패 수")
			.register(meterRegistry)
			.increment();
	}

	/**
	 * 캐시 동기화 스킵
	 */
	public void recordCacheSyncSkipped(String eventType) {
		Counter.builder("cache.sync.skipped")
			.tag("event_type", eventType)
			.tag("tier", "event_consumer")
			.description("멱등성으로 스킵된 캐시 동기화 수")
			.register(meterRegistry)
			.increment();
	}

	// ==================== CacheWarmingScheduler 메트릭 ====================

	/**
	 * Cache Warming 완료
	 */
	public void recordCacheWarmingCompleted(int successCount, int failedCount, long durationMs) {
		Counter.builder("cache.warming.success")
			.tag("tier", "scheduler")
			.description("Cache Warming 성공 건수")
			.register(meterRegistry)
			.increment(successCount);

		Counter.builder("cache.warming.failure")
			.tag("tier", "scheduler")
			.description("Cache Warming 실패 건수")
			.register(meterRegistry)
			.increment(failedCount);

		Timer.builder("cache.warming.duration")
			.tag("tier", "scheduler")
			.description("Cache Warming 소요 시간")
			.register(meterRegistry)
			.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	/**
	 * Cache Warming 실패
	 */
	public void recordCacheWarmingFailed() {
		Counter.builder("cache.warming.execution.failure")
			.tag("tier", "scheduler")
			.description("Cache Warming 전체 실패 수")
			.register(meterRegistry)
			.increment();
	}

	/**
	 * Hot Data 갱신 완료
	 */
	public void recordHotDataRefreshCompleted(int successCount, int failedCount, long durationMs) {
		Counter.builder("cache.hot_data.refresh.success")
			.tag("tier", "scheduler")
			.description("Hot Data 갱신 성공 건수")
			.register(meterRegistry)
			.increment(successCount);

		Counter.builder("cache.hot_data.refresh.failure")
			.tag("tier", "scheduler")
			.description("Hot Data 갱신 실패 건수")
			.register(meterRegistry)
			.increment(failedCount);

		Timer.builder("cache.hot_data.refresh.duration")
			.tag("tier", "scheduler")
			.description("Hot Data 갱신 소요 시간")
			.register(meterRegistry)
			.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	/**
	 * Hot Data 갱신 실패
	 */
	public void recordHotDataRefreshFailed() {
		Counter.builder("cache.hot_data.refresh.execution.failure")
			.tag("tier", "scheduler")
			.description("Hot Data 갱신 전체 실패 수")
			.register(meterRegistry)
			.increment();
	}

	/**
	 * Cold Data 정리 완료
	 */
	public void recordColdDataCleanupCompleted(int deletedCount, int failedCount, long durationMs) {
		Counter.builder("cache.cold_data.cleanup.success")
			.tag("tier", "scheduler")
			.description("Cold Data 정리 성공 건수")
			.register(meterRegistry)
			.increment(deletedCount);

		Counter.builder("cache.cold_data.cleanup.failure")
			.tag("tier", "scheduler")
			.description("Cold Data 정리 실패 건수")
			.register(meterRegistry)
			.increment(failedCount);

		Timer.builder("cache.cold_data.cleanup.duration")
			.tag("tier", "scheduler")
			.description("Cold Data 정리 소요 시간")
			.register(meterRegistry)
			.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	/**
	 * Cold Data 정리 실패
	 */
	public void recordColdDataCleanupFailed() {
		Counter.builder("cache.cold_data.cleanup.execution.failure")
			.tag("tier", "scheduler")
			.description("Cold Data 정리 전체 실패 수")
			.register(meterRegistry)
			.increment();
	}
}
