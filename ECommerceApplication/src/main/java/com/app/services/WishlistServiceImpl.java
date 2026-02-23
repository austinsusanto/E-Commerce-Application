package com.app.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entites.Product;
import com.app.entites.User;
import com.app.entites.Wishlist;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.ProductDTO;
import com.app.payloads.WishlistDTO;
import com.app.repositories.ProductRepo;
import com.app.repositories.UserRepo;
import com.app.repositories.WishlistRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class WishlistServiceImpl implements WishlistService {

	@Autowired
	private WishlistRepo wishlistRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private ProductRepo productRepo;
	
	@Autowired
	private ModelMapper modelMapper;

	@Override
	public WishlistDTO addProductToWishlist(Long userId, Long productId) {
		
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
		
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
		
		if (wishlistRepo.existsByUserUserIdAndProductProductId(userId, productId)) {
			throw new APIException("Product " + product.getProductName() + " already exists in wishlist");
		}
		
		Wishlist wishlist = new Wishlist();
		wishlist.setUser(user);
		wishlist.setProduct(product);
		
		Wishlist savedWishlist = wishlistRepo.save(wishlist);
		
		WishlistDTO wishlistDTO = new WishlistDTO();
		wishlistDTO.setWishlistId(savedWishlist.getWishlistId());
		wishlistDTO.setUserId(userId);
		wishlistDTO.setProductId(product.getProductId());
		wishlistDTO.setProductName(product.getProductName());
		wishlistDTO.setProductPrice(product.getSpecialPrice());
		wishlistDTO.setProductImage(product.getImage());
		wishlistDTO.setAddedAt(savedWishlist.getAddedAt());
		
		return wishlistDTO;
	}

	@Override
	public List<WishlistDTO> getAllWishlistItems(Long userId) {
		
		userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
		
		List<Wishlist> wishlists = wishlistRepo.findByUserUserIdOrderByAddedAtDesc(userId);
		
		if (wishlists.isEmpty()) {
			throw new APIException("No items found in wishlist");
		}
		
		List<WishlistDTO> wishlistDTOs = wishlists.stream().map(wishlist -> {
			WishlistDTO dto = new WishlistDTO();
			dto.setWishlistId(wishlist.getWishlistId());
			dto.setUserId(userId);
			dto.setProductId(wishlist.getProduct().getProductId());
			dto.setProductName(wishlist.getProduct().getProductName());
			dto.setProductPrice(wishlist.getProduct().getSpecialPrice());
			dto.setProductImage(wishlist.getProduct().getImage());
			dto.setAddedAt(wishlist.getAddedAt());
			return dto;
		}).collect(Collectors.toList());
		
		return wishlistDTOs;
	}

	@Override
	public String removeProductFromWishlist(Long userId, Long productId) {
		
		productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
		
		if (!wishlistRepo.existsByUserUserIdAndProductProductId(userId, productId)) {
			throw new ResourceNotFoundException("Wishlist item", "userId and productId", userId + " & " + productId);
		}
		
		wishlistRepo.deleteByUserUserIdAndProductProductId(userId, productId);
		
		return "Product removed from wishlist";
	}

	@Override
	public boolean isProductInWishlist(Long userId, Long productId) {
		
		userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
		
		productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
		
		return wishlistRepo.existsByUserUserIdAndProductProductId(userId, productId);
	}

	@Override
	public void clearWishlist(Long userId) {
		
		userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
		
		List<Wishlist> wishlists = wishlistRepo.findByUserUserIdOrderByAddedAtDesc(userId);
		
		if (wishlists.isEmpty()) {
			throw new APIException("Wishlist is already empty");
		}
		
		wishlistRepo.deleteByUserUserId(userId);
	}
}
