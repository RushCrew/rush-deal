package com.rushcrew.timedeal.application.command;

import com.rushcrew.timedeal.presentation.dto.request.CreateStockRequest;
import java.util.UUID;

public record CreateStockCommand(
    UUID productId, // timeDealProductId
    Long totalStock
) {

    public static CreateStockCommand from(CreateStockRequest request) {
        return new CreateStockCommand(
            request.productId(),
            request.totalStock()
        );
    }
}
