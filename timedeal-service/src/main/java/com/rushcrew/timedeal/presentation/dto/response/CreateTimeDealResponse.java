package com.rushcrew.timedeal.presentation.dto.response;

import com.rushcrew.timedeal.application.result.CreateTimeDealResult;
import java.util.List;
import java.util.UUID;

public record CreateTimeDealResponse(
    UUID timeDealId,
    List<UUID> timeDealProductIds
) {

    public static CreateTimeDealResponse from(CreateTimeDealResult result) {
        return new CreateTimeDealResponse(
            result.timeDealId(),
            result.timeDealProductIds()
        );
    }
}
