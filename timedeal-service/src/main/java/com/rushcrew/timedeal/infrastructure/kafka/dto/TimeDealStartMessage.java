package com.rushcrew.timedeal.infrastructure.kafka.dto;

import java.time.Instant;
import java.util.UUID;

public record TimeDealStartMessage(
    UUID timeDealId,
    Instant startAt
) {

}
