package com.rushcrew.payment_service.presentation.dto.response;

import com.rushcrew.payment_service.application.result.PaymentPrepareResult;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentPrepareResponse(
        UUID paymentId,
        String portOnePaymentId,
        Long amount,
        String status
) {
    public static PaymentPrepareResponse from(PaymentPrepareResult result) {
        return new PaymentPrepareResponse(
                result.paymentId(),
                result.portOnePaymentId(),
                result.amount(),
                result.status()
        );
    }
}
