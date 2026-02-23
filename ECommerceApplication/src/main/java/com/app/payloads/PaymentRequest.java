package com.app.payloads;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
	@NotBlank(message = "Payment method is required")
	private String paymentMethod;
	
	private String cardNumber;
	private String cvc;
	
	// COD fields
	private Long addressId;
	private String membershipCode;
}
