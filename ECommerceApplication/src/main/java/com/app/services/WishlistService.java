package com.app.services;

import java.util.List;

import com.app.payloads.WishlistDTO;

public interface WishlistService {
	
	WishlistDTO addProductToWishlist(Long userId, Long productId);
	
	List<WishlistDTO> getAllWishlistItems(Long userId);
	
	String removeProductFromWishlist(Long userId, Long productId);
	
	boolean isProductInWishlist(Long userId, Long productId);
	
	void clearWishlist(Long userId);
}
