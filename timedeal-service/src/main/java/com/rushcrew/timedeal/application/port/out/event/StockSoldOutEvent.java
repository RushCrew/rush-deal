package com.rushcrew.timedeal.application.port.out.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockSoldOutEvent(
    UUID productId,
    String status,
    LocalDateTime soldOutAt
) {

}
