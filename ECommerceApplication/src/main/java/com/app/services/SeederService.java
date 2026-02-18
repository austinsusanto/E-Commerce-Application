package com.app.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.Category;
import com.app.entites.Product;
import com.app.entites.Role;
import com.app.entites.StoreDiscount;
import com.app.entites.User;
import com.app.repositories.AddressRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.CategoryRepo;
import com.app.repositories.ProductRepo;
import com.app.repositories.RoleRepo;
import com.app.repositories.StoreDiscountRepo;
import com.app.repositories.UserRepo;

import net.datafaker.Faker;

@Service
@Transactional
public class SeederService {

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private StoreDiscountRepo storeDiscountRepo;

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private AddressRepo addressRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final Faker faker = new Faker();

	public String seedRoles() {
		if (roleRepo.count() > 0) {
			return "Roles already seeded";
		}

		Role adminRole = new Role();
		adminRole.setRoleId(101L);
		adminRole.setRoleName("ADMIN");

		Role userRole = new Role();
		userRole.setRoleId(102L);
		userRole.setRoleName("USER");

		roleRepo.saveAll(List.of(adminRole, userRole));
		return "Seeded 2 roles (ADMIN, USER)";
	}

	public String seedCategories() {
		if (categoryRepo.count() > 0) {
			return "Categories already seeded";
		}

		List<String> categoryNames = List.of("Electronics", "Clothing", "Books", "Home & Garden", "Sports");
		List<Category> categories = new ArrayList<>();

		for (String name : categoryNames) {
			Category category = new Category();
			category.setCategoryName(name);
			categories.add(category);
		}

		categoryRepo.saveAll(categories);
		return "Seeded " + categories.size() + " categories";
	}

	public String seedUsers(int count) {
		Role userRole = roleRepo.findById(102L).orElse(null);
		if (userRole == null) {
			return "Please seed roles first";
		}

		List<User> users = new ArrayList<>();
		String encodedPassword = passwordEncoder.encode("123456");

		for (int i = 0; i < count; i++) {
			User user = new User();
			
			String firstName = faker.name().firstName().replaceAll("[^a-zA-Z]", "");
			while (firstName.length() < 5) {
				firstName += faker.name().firstName().replaceAll("[^a-zA-Z]", "");
			}
			user.setFirstName(firstName.substring(0, Math.min(firstName.length(), 20)));
			
			String lastName = faker.name().lastName().replaceAll("[^a-zA-Z]", "");
			while (lastName.length() < 5) {
				lastName += faker.name().lastName().replaceAll("[^a-zA-Z]", "");
			}
			user.setLastName(lastName.substring(0, Math.min(lastName.length(), 20)));
			
			user.setEmail(faker.internet().emailAddress());
			user.setMobileNumber(faker.number().digits(10));
			user.setPassword(encodedPassword);

			Set<Role> roles = new HashSet<>();
			roles.add(userRole);
			user.setRoles(roles);

			users.add(user);
		}

		userRepo.saveAll(users);
		return "Seeded " + count + " users with password: 123456";
	}

	public String seedAdmin() {
		if (userRepo.findByEmail("admin@example.com").isPresent()) {
			return "Admin already exists";
		}

		Role adminRole = roleRepo.findById(101L).orElse(null);
		if (adminRole == null) {
			return "Please seed roles first";
		}

		User admin = new User();
		admin.setFirstName("Admin");
		admin.setLastName("System");
		admin.setEmail("admin@example.com");
		admin.setMobileNumber("1234567890");
		admin.setPassword(passwordEncoder.encode("123456"));

		Set<Role> roles = new HashSet<>();
		roles.add(adminRole);
		admin.setRoles(roles);

		userRepo.save(admin);
		return "Seeded admin user: admin@example.com / 123456";
	}

	public String seedProducts(int count) {
		List<Category> categories = categoryRepo.findAll();
		if (categories.isEmpty()) {
			return "Please seed categories first";
		}

		List<Product> products = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			Product product = new Product();
			product.setProductName(faker.commerce().productName());
			product.setDescription(faker.lorem().sentence(10));
			product.setImage("https://picsum.photos/seed/" + faker.number().numberBetween(1, 1000) + "/400/400");
			product.setQuantity(faker.number().numberBetween(10, 100));
			product.setPrice(Double.parseDouble(faker.commerce().price(10, 500)));
			product.setDiscount(faker.number().numberBetween(0, 30));
			product.setSpecialPrice(product.getPrice() - (product.getPrice() * product.getDiscount() / 100));
			product.setCategory(categories.get(faker.number().numberBetween(0, categories.size())));

			products.add(product);
		}

		productRepo.saveAll(products);
		return "Seeded " + count + " products";
	}

	public String seedStoreDiscounts() {
		if (storeDiscountRepo.count() > 0) {
			return "Store discounts already seeded";
		}

		List<StoreDiscount> discounts = new ArrayList<>();

		StoreDiscount blackFriday = new StoreDiscount();
		blackFriday.setDiscountName("Black Friday Sale");
		blackFriday.setDiscountValue(25.0);
		blackFriday.setStartTime(LocalDateTime.now());
		blackFriday.setEndTime(LocalDateTime.now().plusDays(7));
		blackFriday.setIsActive(true);
		discounts.add(blackFriday);

		StoreDiscount newYear = new StoreDiscount();
		newYear.setDiscountName("New Year Promo");
		newYear.setDiscountValue(15.0);
		newYear.setStartTime(LocalDateTime.now().plusDays(30));
		newYear.setEndTime(LocalDateTime.now().plusDays(37));
		newYear.setIsActive(false);
		discounts.add(newYear);

		StoreDiscount flash = new StoreDiscount();
		flash.setDiscountName("Flash Sale");
		flash.setDiscountValue(10.0);
		flash.setStartTime(LocalDateTime.now().minusDays(1));
		flash.setEndTime(LocalDateTime.now().plusHours(6));
		flash.setIsActive(true);
		discounts.add(flash);

		storeDiscountRepo.saveAll(discounts);
		return "Seeded " + discounts.size() + " store discounts";
	}

	public String seedCarts() {
		List<User> users = userRepo.findAll();
		if (users.isEmpty()) {
			return "Please seed users first";
		}

		List<Cart> carts = new ArrayList<>();
		for (User user : users) {
			Cart cart = new Cart();
			cart.setUser(user);
			cart.setTotalPrice(0.0);
			carts.add(cart);
		}

		cartRepo.saveAll(carts);
		return "Seeded " + carts.size() + " carts for all users";
	}

	public String seedAddresses() {
		Faker faker = new Faker();
		List<User> users = userRepo.findAll();
		if (users.isEmpty()) {
			return "Please seed users first";
		}

		for (User user : users) {
			Address address = new Address();
			address.setStreet(faker.address().streetAddress());
			address.setBuildingName(faker.address().buildingNumber() + " Building");
			address.setCity(faker.address().city());
			address.setState(faker.address().state());
			address.setCountry(faker.address().country());
			address.setPincode(faker.numerify("######"));
			addressRepo.save(address);
			user.getAddresses().add(address);
			userRepo.save(user);
		}
		return "Seeded addresses for " + users.size() + " users";
	}

	public String seedAll() {
		StringBuilder result = new StringBuilder();
		result.append(seedRoles()).append("\n");
		result.append(seedCategories()).append("\n");
		result.append(seedAdmin()).append("\n");
		result.append(seedUsers(5)).append("\n");
		result.append(seedAddresses()).append("\n");
		result.append(seedCarts()).append("\n");
		result.append(seedProducts(50)).append("\n");
		result.append(seedStoreDiscounts()).append("\n");
		return result.toString();
	}
}
