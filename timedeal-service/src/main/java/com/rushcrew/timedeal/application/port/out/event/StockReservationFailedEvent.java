package com.rushcrew.timedeal.application.port.out.event;

import java.time.Instant;

public record StockReservationFailedEvent(
	String sagaId,
	String productId,
	String reason,
	Instant occurredAt
) {
	public static StockReservationFailedEvent of(
		String sagaId,
		String productId,
		String reason
	) {
		return new StockReservationFailedEvent(
			sagaId,
			productId,
			reason,
			Instant.now()
		);
	}
}
