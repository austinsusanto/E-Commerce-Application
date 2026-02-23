package com.app.payloads;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {
	
	private Long wishlistId;
	private Long userId;
	private Long productId;
	private String productName;
	private Double productPrice;
	private String productImage;
	private Date addedAt;
}
