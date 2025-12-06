package com.rushcrew.order_service.application.port.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockReservationResult {
	private UUID timeDealStockId;
	private Integer quantity;
	private boolean success;
	private Integer availableStock;
	private String message;

	public static StockReservationResult success(UUID timeDealStockId, Integer quantity) {
		return new StockReservationResult(timeDealStockId, quantity, true, null, null);
	}

	public static StockReservationResult failure(Integer availableStock, String message) {
		return new StockReservationResult(null, null, false, availableStock, message);
	}
}
