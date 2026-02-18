package com.app.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.entites.Category;
import com.app.entites.Product;
import com.app.entites.Role;
import com.app.entites.Discount;
import com.app.entites.User;
import com.app.repositories.CategoryRepo;
import com.app.repositories.ProductRepo;
import com.app.repositories.RoleRepo;
import com.app.repositories.DiscountRepo;
import com.app.repositories.UserRepo;

import net.datafaker.Faker;

@Service
public class SeedService {

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private DiscountRepo discountRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker();

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
            userRole = new Role();
            userRole.setRoleId(102L);
            userRole.setRoleName("USER");
        }

        List<User> users = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode("123456");

        for (int i = 0; i < count; i++) {
            User user = new User();
            String firstname = faker.name().firstName();
            if (firstname.length() < 5) {
                firstname = "Brock";
            }
            user.setFirstName(firstname);
            String lastName = faker.name().lastName();
            if (lastName.length() < 5) {
                lastName = "Lesnar";
            }
            user.setLastName(lastName);
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
            adminRole = new Role();
            adminRole.setRoleId(101L);
            adminRole.setRoleName("ADMIN");
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

    public String seedDiscounts() {
        if (discountRepo.count() > 0) {
            return "Store discounts already seeded";
        }

        List<Discount> discounts = new ArrayList<>();

        Discount blackFriday = new Discount();
        blackFriday.setDiscountCode("SPLE_IS_FUN");
        blackFriday.setDiscountValue(25.0);
        blackFriday.setStartTime(LocalDateTime.now());
        blackFriday.setEndTime(LocalDateTime.now().plusDays(7));
        blackFriday.setIsActive(true);
        discounts.add(blackFriday);

        Discount newYear = new Discount();
        newYear.setDiscountCode("RSE_TRAKTIRAN");
        newYear.setDiscountValue(15.0);
        newYear.setStartTime(LocalDateTime.now());
        newYear.setEndTime(LocalDateTime.now().plusDays(14));
        newYear.setIsActive(false);
        discounts.add(newYear);

        discountRepo.saveAll(discounts);
        return "Seeded " + discounts.size() + " store discounts";
    }

    public String seedAll() {
        StringBuilder result = new StringBuilder();
        result.append(seedCategories()).append("\n");
        result.append(seedAdmin()).append("\n");
        result.append(seedUsers(5)).append("\n");
        result.append(seedProducts(20)).append("\n");
        result.append(seedDiscounts()).append("\n");
        return result.toString();
    }
}