package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.payloads.WishlistDTO;
import com.app.payloads.WishlistResponse;
import com.app.services.WishlistService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class WishlistController {
	
	@Autowired
	private WishlistService wishlistService;

	@PostMapping("/public/users/{userId}/wishlist/products/{productId}")
	public ResponseEntity<WishlistDTO> addProductToWishlist(@PathVariable Long userId, @PathVariable Long productId) {
		WishlistDTO wishlistDTO = wishlistService.addProductToWishlist(userId, productId);
		
		return new ResponseEntity<WishlistDTO>(wishlistDTO, HttpStatus.CREATED);
	}
	
	@GetMapping("/public/users/{userId}/wishlist")
	public ResponseEntity<List<WishlistDTO>> getAllWishlistItems(@PathVariable Long userId) {
		List<WishlistDTO> wishlistDTOs = wishlistService.getAllWishlistItems(userId);
		
		return new ResponseEntity<List<WishlistDTO>>(wishlistDTOs, HttpStatus.FOUND);
	}
	
	@DeleteMapping("/public/users/{userId}/wishlist/products/{productId}")
	public ResponseEntity<WishlistResponse> removeProductFromWishlist(@PathVariable Long userId, @PathVariable Long productId) {
		String status = wishlistService.removeProductFromWishlist(userId, productId);
		
		WishlistResponse response = new WishlistResponse(status, true);
		return new ResponseEntity<WishlistResponse>(response, HttpStatus.OK);
	}
	
	@GetMapping("/public/users/{userId}/wishlist/products/{productId}/exists")
	public ResponseEntity<WishlistResponse> isProductInWishlist(@PathVariable Long userId, @PathVariable Long productId) {
		boolean exists = wishlistService.isProductInWishlist(userId, productId);
		
		WishlistResponse response = new WishlistResponse(
			exists ? "Product exists in wishlist" : "Product not found in wishlist", 
			exists
		);
		return new ResponseEntity<WishlistResponse>(response, HttpStatus.OK);
	}
	
	@DeleteMapping("/public/users/{userId}/wishlist")
	public ResponseEntity<WishlistResponse> clearWishlist(@PathVariable Long userId) {
		wishlistService.clearWishlist(userId);
		
		WishlistResponse response = new WishlistResponse("Wishlist cleared successfully", true);
		return new ResponseEntity<WishlistResponse>(response, HttpStatus.OK);
	}
}
