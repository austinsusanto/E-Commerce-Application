package com.app.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Product;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CartDTO;
import com.app.payloads.ProductDTO;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.ProductRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private CartItemRepo cartItemRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public CartDTO addProductToCart(Long cartId, Long productId, Integer quantity) {

		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem != null) {
			throw new APIException("Product " + product.getProductName() + " already exists in the cart");
		}

		if (product.getStock() == null || product.getStock() == 0) {
			throw new APIException(product.getProductName() + " is not available");
		}

		if (product.getStock() == null || product.getStock() < quantity) {
			throw new APIException("Please, make an order of the " + product.getProductName()
					+ " less than or equal to the stock " + product.getStock() + ".");
		}

		CartItem newCartItem = new CartItem();

		newCartItem.setProduct(product);
		newCartItem.setCart(cart);
		newCartItem.setQuantity(quantity);
		newCartItem.setDiscount(product.getDiscount());
		newCartItem.setProductPrice(product.getSpecialPrice());

		cartItemRepo.save(newCartItem);

		cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		List<ProductDTO> productDTOs = cart.getCartItems().stream()
				.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

		cartDTO.setProducts(productDTOs);

		return cartDTO;

	}

	@Override
	public List<CartDTO> getAllCarts() {
		List<Cart> carts = cartRepo.findAll();

		if (carts.size() == 0) {
			throw new APIException("No cart exists");
		}

		List<CartDTO> cartDTOs = carts.stream().map(cart -> {
			CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

			List<ProductDTO> products = cart.getCartItems().stream()
					.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

			cartDTO.setProducts(products);

			return cartDTO;

		}).collect(Collectors.toList());

		return cartDTOs;
	}

	@Override
	public CartDTO getCart(String email, Long cartId) {
		Cart cart = cartRepo.findCartByEmailAndCartId(email, cartId);

		if (cart == null) {
			throw new ResourceNotFoundException("Cart", "cartId", cartId);
		}

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
		
		List<ProductDTO> products = cart.getCartItems().stream()
				.map(p -> {
					ProductDTO dto = modelMapper.map(p.getProduct(), ProductDTO.class);
					dto.setStock(p.getProduct().getStock());
					return dto;
				}).collect(Collectors.toList());

		cartDTO.setProducts(products);

		return cartDTO;
	}

	@Override
	public void updateProductInCarts(Long cartId, Long productId) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem == null) {
			throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
		}

		double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

		cartItem.setProductPrice(product.getSpecialPrice());

		cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

		cartItem = cartItemRepo.save(cartItem);
	}

	@Override
	public CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		if (product.getStock() == null || product.getStock() == 0) {
			throw new APIException(product.getProductName() + " is not available");
		}

		if (product.getStock() == null || product.getStock() < quantity) {
			throw new APIException("Please, make an order of the " + product.getProductName()
					+ " less than or equal to the stock " + product.getStock() + ".");
		}

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem == null) {
			throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
		}

		double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

		cartItem.setProductPrice(product.getSpecialPrice());
		cartItem.setQuantity(quantity);
		cartItem.setDiscount(product.getDiscount());

		cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * quantity));

		cartItem = cartItemRepo.save(cartItem);

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		List<ProductDTO> productDTOs = cart.getCartItems().stream()
				.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

		cartDTO.setProducts(productDTOs);

		return cartDTO;

	}

	@Override
	public String deleteProductFromCart(Long cartId, Long productId) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem == null) {
			throw new ResourceNotFoundException("Product", "productId", productId);
		}

		cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

		cartItemRepo.deleteCartItemByProductIdAndCartId(cartId, productId);

		return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
	}

}
