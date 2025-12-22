package com.rushcrew.order_service.infrastructure.adapter.out.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.rushcrew.order_service.infrastructure.dto.payment.PaymentCancelRequest;
import com.rushcrew.order_service.infrastructure.dto.payment.PaymentPrepareResponse;
import com.rushcrew.order_service.infrastructure.dto.payment.PaymentRequest;

@FeignClient(name = "payment-service", url = "${PAYMENT_SERVICE_URL}")
public interface PaymentFeignClient {
	@PostMapping("/api/v1/payments")
	PaymentPrepareResponse requestPayment(@RequestBody PaymentRequest request);

	@PostMapping("/api/v1/payments/cancel")
	void cancelPayment(@RequestBody PaymentCancelRequest request);
}
