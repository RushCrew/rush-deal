package com.rushcrew.payment_service.presentation.dto.request;

import com.rushcrew.payment_service.application.command.PaymentCommand;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,
        @NotNull(message = "결제 금액은 필수입니다.")
        Long totalAmount
) {
        public PaymentCommand toCommand() {
                return new PaymentCommand(
                        this.orderId,
                        this.totalAmount
                );
        }
}
