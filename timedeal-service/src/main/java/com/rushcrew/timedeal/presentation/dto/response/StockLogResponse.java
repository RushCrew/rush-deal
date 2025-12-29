package com.rushcrew.timedeal.presentation.dto.response;

import com.rushcrew.timedeal.application.result.StockLogResult;
import java.util.UUID;

public record StockLogResponse(
    UUID stockLogId,
    UUID timeDealStockId,
    UUID orderId,
    String eventType,
    Long quantity,
    String description
) {

    public static StockLogResponse from(StockLogResult stockLogResult) {
        return new StockLogResponse(
            stockLogResult.stockLogId(),
            stockLogResult.timeDealStockId(),
            stockLogResult.orderId(),
            stockLogResult.eventType().name(),
            stockLogResult.quantity(),
            stockLogResult.description()
        );
    }
}
