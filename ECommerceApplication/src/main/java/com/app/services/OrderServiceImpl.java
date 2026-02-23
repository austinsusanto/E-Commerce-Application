package com.app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.MemberDiscount;
import com.app.entites.Order;
import com.app.entites.OrderItem;
import com.app.entites.Payment;
import com.app.entites.Product;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.OrderDTO;
import com.app.payloads.OrderItemDTO;
import com.app.payloads.OrderResponse;
import com.app.payloads.PaymentRequest;
import com.app.repositories.AddressRepo;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.MemberDiscountRepo;
import com.app.repositories.OrderItemRepo;
import com.app.repositories.OrderRepo;
import com.app.repositories.PaymentRepo;
import com.app.repositories.StoreDiscountRepo;
import com.app.repositories.UserRepo;
import com.app.entites.StoreDiscount;
import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	public UserRepo userRepo;

	@Autowired
	public CartRepo cartRepo;

	@Autowired
	public OrderRepo orderRepo;

	@Autowired
	private PaymentRepo paymentRepo;

	@Autowired
	public OrderItemRepo orderItemRepo;

	@Autowired
	public CartItemRepo cartItemRepo;

	@Autowired
	public DiscountRepo discountRepo;

	@Autowired
	public UserService userService;

	@Autowired
	public CartService cartService;

	@Autowired
	public ModelMapper modelMapper;

	@Autowired
	public StoreDiscountRepo storeDiscountRepo;
	public AddressRepo addressRepo;

	@Autowired
	public MemberDiscountRepo memberDiscountRepo;

	@Override
	public OrderDTO placeOrder(String email, Long cartId, PaymentRequest paymentRequest) {

		Cart cart = cartRepo.findCartByEmailAndCartId(email, cartId);

		if (cart == null) {
			throw new ResourceNotFoundException("Cart", "cartId", cartId);
		}

		List<CartItem> cartItems = cart.getCartItems();

		if (cartItems.size() == 0) {
			throw new APIException("Cart is empty");
		}

		for (CartItem cartItem : cartItems) {
			Product product = cartItem.getProduct();
			int quantity = cartItem.getQuantity();
			if (product.getStock() == null || product.getStock() < quantity) {
				throw new APIException("Insufficient stock for " + product.getProductName()
						+ ". Available stock: " + (product.getStock() == null ? 0 : product.getStock()) + ".");
			}
		}

		Order order = new Order();
		order.setEmail(email);
		order.setOrderDate(LocalDate.now());
		order.setOrderStatus("Order Accepted !");

		// Handle COD specific validations
		Address deliveryAddress = null;
		MemberDiscount memberDiscount = null;
		double memberDiscountPercentage = 0;

		if ("Cash On Delivery".equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
			if (paymentRequest.getAddressId() == null) {
				throw new APIException("Delivery address is required for COD payment");
			}

			deliveryAddress = addressRepo.findById(paymentRequest.getAddressId())
					.orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", paymentRequest.getAddressId()));

			if (paymentRequest.getMembershipCode() != null && !paymentRequest.getMembershipCode().isBlank()) {
				memberDiscount = memberDiscountRepo.findByMembershipCode(paymentRequest.getMembershipCode())
						.orElseThrow(() -> new APIException("Invalid membership code: " + paymentRequest.getMembershipCode()));

				if (!memberDiscount.isActive()) {
					throw new APIException("Membership code is no longer active");
				}

				memberDiscountPercentage = memberDiscount.getDiscountPercentage();
				order.setMembershipCode(paymentRequest.getMembershipCode());
				order.setMemberDiscount(memberDiscountPercentage);
			}

			order.setDeliveryAddress(deliveryAddress);
		} else {
			// Credit Card validations
			if ("Credit Card".equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
				if (paymentRequest.getCardNumber() == null || paymentRequest.getCardNumber().isBlank()) {
					throw new APIException("Card number is required for credit card payment");
				}
				if (paymentRequest.getCvc() == null || paymentRequest.getCvc().isBlank()) {
					throw new APIException("CVC is required for credit card payment");
				}
			}
		}

		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setPaymentMethod(paymentRequest.getPaymentMethod());
		payment.setCardNumber(paymentRequest.getCardNumber());
		payment.setCvc(paymentRequest.getCvc());

		payment = paymentRepo.save(payment);
		order.setPayment(payment);

		// Store discount logic
		List<StoreDiscount> activeDiscounts = storeDiscountRepo.findActiveDiscountsByTime(LocalDateTime.now());
		StoreDiscount activeStoreDiscount = activeDiscounts.isEmpty() ? null : activeDiscounts.get(0);
		
		if (activeStoreDiscount != null) {
			order.setStoreDiscount(activeStoreDiscount);
		}

		Order savedOrder = orderRepo.save(order);

		List<OrderItem> orderItems = new ArrayList<>();
		double totalAmount = 0;

		for (CartItem cartItem : cartItems) {
			OrderItem orderItem = new OrderItem();
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setOrder(savedOrder);

			double price = cartItem.getProduct().getPrice();

			// Apply discounts in priority order: Member > Store > Cart
			if (memberDiscount != null && "Cash On Delivery".equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
				double discountedPrice = price - (price * memberDiscountPercentage / 100);
				orderItem.setDiscount(memberDiscountPercentage);
				orderItem.setOrderedProductPrice(discountedPrice);
				totalAmount += discountedPrice * cartItem.getQuantity();
			} else if (activeStoreDiscount != null) {
				double discountPercent = activeStoreDiscount.getDiscountValue();
				double discountAmount = price * (discountPercent / 100);
				double finalPrice = price - discountAmount;
				orderItem.setDiscount(discountPercent);
				orderItem.setOrderedProductPrice(finalPrice);
				totalAmount += finalPrice * cartItem.getQuantity();
			} else {
				orderItem.setDiscount(cartItem.getDiscount());
				orderItem.setOrderedProductPrice(cartItem.getProductPrice());
				totalAmount += cartItem.getProductPrice() * cartItem.getQuantity();
			}

			orderItems.add(orderItem);
		}

		savedOrder.setTotalAmount(totalAmount);
		orderItems = orderItemRepo.saveAll(orderItems);
		savedOrder = orderRepo.save(savedOrder);

		final Long finalCartId = cartId;
		cart.getCartItems().forEach(item -> {
			int quantity = item.getQuantity();
			Product product = item.getProduct();
			cartService.deleteProductFromCart(finalCartId, item.getProduct().getProductId());
			product.setStock(product.getStock() - quantity);
		});

		OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
		orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

		return orderDTO;
	}

	@Override
	public List<OrderDTO> getOrdersByUser(String email) {
		List<Order> orders = orderRepo.findAllByEmail(email);

		List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
				.collect(Collectors.toList());

		if (orderDTOs.size() == 0) {
			throw new APIException("No orders placed yet by the user with email: " + email);
		}

		return orderDTOs;
	}

	@Override
	public OrderDTO getOrder(String email, Long orderId) {

		Order order = orderRepo.findOrderByEmailAndOrderId(email, orderId);

		if (order == null) {
			throw new ResourceNotFoundException("Order", "orderId", orderId);
		}

		return modelMapper.map(order, OrderDTO.class);
	}

	@Override
	public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();

		Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

		Page<Order> pageOrders = orderRepo.findAll(pageDetails);

		List<Order> orders = pageOrders.getContent();

		List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
				.collect(Collectors.toList());

		if (orderDTOs.size() == 0) {
			throw new APIException("No orders placed yet by the users");
		}

		OrderResponse orderResponse = new OrderResponse();

		orderResponse.setContent(orderDTOs);
		orderResponse.setPageNumber(pageOrders.getNumber());
		orderResponse.setPageSize(pageOrders.getSize());
		orderResponse.setTotalElements(pageOrders.getTotalElements());
		orderResponse.setTotalPages(pageOrders.getTotalPages());
		orderResponse.setLastPage(pageOrders.isLast());

		return orderResponse;
	}

	@Override
	public OrderDTO updateOrder(String email, Long orderId, String orderStatus) {

		Order order = orderRepo.findOrderByEmailAndOrderId(email, orderId);

		if (order == null) {
			throw new ResourceNotFoundException("Order", "orderId", orderId);
		}

		order.setOrderStatus(orderStatus);

		return modelMapper.map(order, OrderDTO.class);
	}

}
