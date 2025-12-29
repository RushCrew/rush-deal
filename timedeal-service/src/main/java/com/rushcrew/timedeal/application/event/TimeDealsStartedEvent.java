package com.rushcrew.timedeal.application.event;

import java.time.Instant;
import java.util.Map;

public record TimeDealsStartedEvent(
    Map<String, Instant> timeDealStartMap
) {

}
