package com.rushcrew.timedeal.application.result;

import com.rushcrew.timedeal.domain.entity.TimeDeal;
import com.rushcrew.timedeal.domain.entity.TimeDealProduct;
import java.util.List;
import java.util.UUID;

public record CreateTimeDealResult(
    UUID timeDealId,
    List<UUID> timeDealProductIds
) {

    public static CreateTimeDealResult from(TimeDeal timeDeal) {
        List<UUID> timeDealProductIds = timeDeal.getTimeDealProducts().stream()
            .map(TimeDealProduct::getId).toList();
        return new CreateTimeDealResult(
            timeDeal.getId(),
            timeDealProductIds
        );
    }
}
