package com.rushcrew.queue.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 재고 품절 이벤트
 * TimeDeal Service와 Queue Service가 서로 주고받을 메시지 규격
 * @param productId
 * @param status
 */
public record SoldOutEvent(
    UUID productId,
    String status,
    LocalDateTime soldOutAt
) {

}
