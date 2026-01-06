package com.rushcrew.payment_service.application.result;

import com.rushcrew.payment_service.domain.model.Payment;
import com.rushcrew.payment_service.domain.vo.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentPrepareResult(
        UUID paymentId,
        String portOnePaymentId,
        Long amount,
        String status
) {
    public static PaymentPrepareResult of(String portOnePaymentId, Payment payment) {
        return new PaymentPrepareResult(
                payment.getPaymentId(),
                portOnePaymentId,
                payment.getAmount(),
                payment.getStatus().getDescription()
        );
    }
}
