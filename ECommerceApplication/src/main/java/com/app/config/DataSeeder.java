package com.app.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.Category;
import com.app.entites.MemberDiscount;
import com.app.entites.Product;
import com.app.entites.Role;
import com.app.entites.User;
import com.app.repositories.AddressRepo;
import com.app.repositories.CategoryRepo;
import com.app.repositories.MemberDiscountRepo;
import com.app.repositories.ProductRepo;
import com.app.repositories.RoleRepo;
import com.app.repositories.UserRepo;

import jakarta.transaction.Transactional;

@Component
@Order(2)
public class DataSeeder implements CommandLineRunner {

	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private AddressRepo addressRepo;

	@Autowired
	private MemberDiscountRepo memberDiscountRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		if (categoryRepo.count() > 0) {
			return;
		}

		try {
			seedData();
			System.out.println("Data seeding completed successfully!");
		} catch (Exception e) {
			System.out.println("Data seeding skipped (data may already exist): " + e.getMessage());
		}
	}

	private void seedData() {
		Category electronics = new Category();
		electronics.setCategoryName("Electronics");

		Category clothing = new Category();
		clothing.setCategoryName("Clothing");

		Category books = new Category();
		books.setCategoryName("Books");

		Category homeAppliances = new Category();
		homeAppliances.setCategoryName("Home Appliances");

		categoryRepo.saveAll(List.of(electronics, clothing, books, homeAppliances));

		Product laptop = createProduct("Laptop Gaming ASUS", "High performance gaming laptop with RTX 4060", 15, 15000000, 10, electronics);
		Product smartphone = createProduct("Samsung Galaxy S24", "Latest Samsung flagship smartphone", 25, 12000000, 5, electronics);
		Product headphones = createProduct("Sony WH-1000XM5", "Premium noise cancelling wireless headphones", 30, 4500000, 15, electronics);

		Product tshirt = createProduct("Kaos Polos Premium", "Kaos polos bahan cotton combed 30s", 100, 150000, 10, clothing);
		Product jeans = createProduct("Celana Jeans Slim Fit", "Celana jeans slim fit bahan stretch", 50, 350000, 20, clothing);
		Product jacket = createProduct("Jaket Hoodie Fleece", "Jaket hoodie bahan fleece tebal dan hangat", 40, 250000, 15, clothing);

		Product javaBook = createProduct("Java Programming", "Complete guide to Java programming language", 20, 120000, 5, books);
		Product springBook = createProduct("Spring Boot in Action", "Learn Spring Boot framework from scratch", 15, 180000, 10, books);

		Product blender = createProduct("Blender Philips", "Blender multi-fungsi dengan 3 kecepatan", 20, 500000, 10, homeAppliances);
		Product riceCooker = createProduct("Rice Cooker Miyako", "Rice cooker 1.8 liter anti lengket", 15, 350000, 5, homeAppliances);

		productRepo.saveAll(List.of(laptop, smartphone, headphones, tshirt, jeans, jacket, javaBook, springBook, blender, riceCooker));

		Address address1 = new Address();
        address1.setStreet("Jalan Sudirman No. 1");
        address1.setBuildingName("Gedung A Lt. 5");
        address1.setCity("Jakarta Selatan");
        address1.setState("DKI Jakarta");
        address1.setCountry("Indonesia");
        address1.setPincode("123456");

        Address address2 = new Address();
        address2.setStreet("Jalan Margonda Raya");
        address2.setBuildingName("Kampus UI Depok");
        address2.setCity("Depok");
        address2.setState("Jawa Barat");
        address2.setCountry("Indonesia");
        address2.setPincode("164230");

        Address address3 = new Address();
        address3.setStreet("Jalan Pemuda No. 10");
        address3.setBuildingName("Gedung Baru Lt. 3");
        address3.setCity("Surabaya");
        address3.setState("Jawa Timur");
        address3.setCountry("Indonesia");
        address3.setPincode("601234");

		addressRepo.saveAll(List.of(address1, address2, address3));

		Role adminRole = roleRepo.findById(AppConstants.ADMIN_ID).orElse(null);
		Role userRole = roleRepo.findById(AppConstants.USER_ID).orElse(null);

		if (adminRole != null && userRole != null) {
			User admin = new User();
			admin.setFirstName("Admin");
			admin.setLastName("Utama");
			admin.setEmail("admin@ecommerce.com");
			admin.setMobileNumber("0812345678");
			admin.setPassword(passwordEncoder.encode("admin123"));
			admin.getRoles().add(adminRole);
			admin.setAddresses(List.of(address1));
			Cart adminCart = new Cart();
			adminCart.setUser(admin);
			admin.setCart(adminCart);
			userRepo.save(admin);

			User user1 = new User();
			user1.setFirstName("Austin");
			user1.setLastName("Susanto");
			user1.setEmail("austin@gmail.com");
			user1.setMobileNumber("0856789012");
			user1.setPassword(passwordEncoder.encode("user12345"));
			user1.getRoles().add(userRole);
			user1.setAddresses(List.of(address2));
			Cart user1Cart = new Cart();
			user1Cart.setUser(user1);
			user1.setCart(user1Cart);
			userRepo.save(user1);

			User user2 = new User();
			user2.setFirstName("Sitii");
			user2.setLastName("Aminah");
			user2.setEmail("siti@gmail.com");
			user2.setMobileNumber("0878901234");
			user2.setPassword(passwordEncoder.encode("user12345"));
			user2.getRoles().add(userRole);
			user2.setAddresses(List.of(address3));
			Cart user2Cart = new Cart();
			user2Cart.setUser(user2);
			user2.setCart(user2Cart);
			userRepo.save(user2);
		}

		MemberDiscount gold = new MemberDiscount();
		gold.setMembershipCode("GOLD2025");
		gold.setMemberName("Gold Member");
		gold.setDiscountPercentage(15);
		gold.setActive(true);

		MemberDiscount silver = new MemberDiscount();
		silver.setMembershipCode("SILVER2025");
		silver.setMemberName("Silver Member");
		silver.setDiscountPercentage(10);
		silver.setActive(true);

		MemberDiscount bronze = new MemberDiscount();
		bronze.setMembershipCode("BRONZE2025");
		bronze.setMemberName("Bronze Member");
		bronze.setDiscountPercentage(5);
		bronze.setActive(true);

		MemberDiscount expired = new MemberDiscount();
		expired.setMembershipCode("EXPIRED01");
		expired.setMemberName("Expired Member");
		expired.setDiscountPercentage(20);
		expired.setActive(false);

		memberDiscountRepo.saveAll(List.of(gold, silver, bronze, expired));
	}

	private Product createProduct(String name, String description, int stock, double price, double discount, Category category) {
		Product product = new Product();
		product.setProductName(name);
		product.setDescription(description);
		product.setStock(stock);
		product.setPrice(price);
		product.setDiscount(discount);
		product.setSpecialPrice(price - (price * discount / 100));
		product.setImage("default.png");
		product.setCategory(category);
		return product;
	}
}
