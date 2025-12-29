package com.rushcrew.timedeal.application.result;

import com.rushcrew.timedeal.domain.vo.EventType;
import java.util.UUID;

public record StockLogResult(
    UUID stockLogId,
    UUID timeDealStockId,
    UUID orderId,
    EventType eventType,
    Long quantity,
    String description
) {

}
