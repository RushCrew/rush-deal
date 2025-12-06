package com.rushcrew.order_service.application.port.out;

import java.util.UUID;

import lombok.NonNull;

public interface QueuePort {

	// 유저의 대기열 토큰을 가져온다.
	// 실제 Validation은 QueueTokenValidator가 수행
	String getUserQueueToken(UUID timeDealId, Long userId);

	/**
	 * 대기열 토큰 검증
	 */
	boolean validateToken(@NonNull UUID timeDealId, @NonNull Long userId);

	/**
	 * 대기열 토큰 TTL 연장
	 */
	void extendTokenTtl(@NonNull UUID timeDealId, @NonNull Long userId, @NonNull Integer ttlSeconds);
}
