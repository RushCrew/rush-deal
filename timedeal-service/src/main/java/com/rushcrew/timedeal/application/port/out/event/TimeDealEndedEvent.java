package com.rushcrew.timedeal.application.port.out.event;

import java.time.Instant;
import java.util.UUID;

public record TimeDealEndedEvent(
    UUID timeDealId,
    Instant endAt
) {

}
