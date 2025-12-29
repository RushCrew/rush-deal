package com.rushcrew.order_service.application.command.port.out;

import java.util.UUID;

import com.rushcrew.order_service.application.query.dto.OrderDetailDto;

public interface OrderCachePort {
	/**
	 * 주문 캐시 업데이트
	 *
	 * 호출 시점:
	 * - 주문 생성/수정 시
	 * - 이벤트 수신 시 (OrderEventConsumer)
	 * - 정기 갱신 시 (CacheWarmingScheduler)
	 */
	void updateOrderCache(UUID orderId, OrderDetailDto orderDetailDto);

	/**
	 * 캐시 존재 여부 확인
	 *
	 * 용도:
	 * - 멱등성 체크 (중복 캐싱 방지)
	 * - OrderEventConsumer에서 사용
	 */
	boolean existsInCache(UUID orderId);

	/**
	 * 주문 캐시 삭제
	 *
	 * 호출 시점:
	 * - Cold Data 정리 시 (CacheWarmingScheduler)
	 * - 주문 삭제 시 (있다면)
	 */
	void evictOrderCache(UUID orderId);
}
