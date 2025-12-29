package com.rushcrew.order_service.infrastructure.dto.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentPrepareResponse(
	UUID paymentId,
	String portOnePaymentId,
	BigDecimal amount,
	String status
) {}
