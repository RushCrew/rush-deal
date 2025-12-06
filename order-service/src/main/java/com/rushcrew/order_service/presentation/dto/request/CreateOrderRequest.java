package com.rushcrew.order_service.presentation.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.rushcrew.order_service.domain.vo.ShippingInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(

	@NotBlank(message = "타임딜 ID는 필수입니다")
	String timeDealId,

	@NotNull(message = "주문 아이템은 필수입니다")
	@Size(min = 1, message = "최소 1개 이상의 상품이 필요합니다")
	@Valid
	List<OrderItemRequest> orderItems,

	@NotNull(message = "포인트 사용량은 필수입니다")
	@Min(value = 0, message = "포인트 사용량은 0 이상이어야 합니다")
	BigDecimal pointUsed,

	@NotNull(message = "배송 정보는 필수입니다")
	@Valid
	ShippingInfoRequest shippingInfo
) {

	public record OrderItemRequest(

		@NotBlank(message = "타임딜 재고 ID는 필수입니다")
		String timeDealStockId,

		@NotNull(message = "수량은 필수입니다")
		@Min(value = 1, message = "수량은 1개 이상이어야 합니다")
		Integer quantity
	) {}

	public record ShippingInfoRequest(

		@NotBlank(message = "수령인 이름은 필수입니다")
		@Size(max = 50, message = "수령인 이름은 50자 이내여야 합니다")
		String recipientName,

		@NotBlank(message = "수령인 연락처는 필수입니다")
		@Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
		String recipientPhone,

		@NotBlank(message = "우편번호는 필수입니다")
		@Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다")
		String zipCode,

		@NotBlank(message = "기본 주소는 필수입니다")
		@Size(max = 255, message = "기본 주소는 255자 이내여야 합니다")
		String addressBase,

		@NotBlank(message = "상세 주소는 필수입니다")
		@Size(max = 255, message = "상세 주소는 255자 이내여야 합니다")
		String addressDetail,

		@Size(max = 100, message = "배송 메시지는 100자 이내여야 합니다")
		String deliveryMessage
	) {
		public ShippingInfo toShippingInfo() {
			return ShippingInfo.builder()
				.recipientName(recipientName)
				.recipientPhone(recipientPhone)
				.zipCode(zipCode)
				.addressBase(addressBase)
				.addressDetail(addressDetail)
				.deliveryMessage(deliveryMessage)
				.build();
		}
	}
}
