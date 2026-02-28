package com.stepup.config;

import com.stepup.model.Product;
import com.stepup.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.stepup.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.stepup.repository.CouponRepository couponRepository;

    @Autowired
    private com.stepup.repository.OfferRepository offerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== DataInitializer: Starting data initialization ===");

        // 1. Seed products
        try {
            long productCount = productRepository.count();
            log.info("DataInitializer: Current product count = {}", productCount);
            if (productCount == 0) {
                createProduct("Whitby Girls White Multi Marble EVA Clog", new java.math.BigDecimal("2799.0"),
                        "images/kids1.png", "Kids", 5);
                createProduct("Boys Blue Sporty Running Shoes", new java.math.BigDecimal("1499.0"),
                        "images/kids2.jpg", "Kids", 4);

                createProduct("Ophelia Womens Silver Diamante Heel", new java.math.BigDecimal("2299.0"),
                        "images/women1.jpg", "Womens", 5);
                createProduct("Classic Pink Canvas Slip-ons", new java.math.BigDecimal("1299.0"),
                        "images/women2.png", "Womens", 4);

                createProduct("Premium Leather Oxford Shoes", new java.math.BigDecimal("4999.0"),
                        "images/mens1.png", "Mens", 5);
                createProduct("Urban Street Style Sneakers", new java.math.BigDecimal("2199.0"),
                        "images/mens2.png", "Mens", 4);

                log.info("DataInitializer: {} products seeded successfully!", productRepository.count());
            } else {
                log.info("DataInitializer: Products already exist, skipping seed.");
            }
        } catch (Exception e) {
            log.error("DataInitializer: FAILED to seed products!", e);
        }

        // 2. Seed users
        try {
            if (userRepository.findByEmail("user@stepup.com").isEmpty()) {
                com.stepup.model.User user = new com.stepup.model.User();
                user.setEmail("user@stepup.com");
                user.setMobileNumber("9999988888");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setName("Test User");
                user.setRole(com.stepup.model.User.Role.USER);
                userRepository.save(user);
                log.info("DataInitializer: Test user created (user@stepup.com / user123)");
            }
            if (userRepository.findByEmail("admin@stepup.com").isEmpty()) {
                com.stepup.model.User admin = new com.stepup.model.User();
                admin.setEmail("admin@stepup.com");
                admin.setMobileNumber("0000000000");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setName("Admin User");
                admin.setRole(com.stepup.model.User.Role.ADMIN);
                userRepository.save(admin);
                log.info("DataInitializer: Admin user created (admin@stepup.com / admin123)");
            }
        } catch (Exception e) {
            log.error("DataInitializer: FAILED to seed users!", e);
        }

        // 3. Seed coupons and offers
        try {
            if (couponRepository.count() == 0) {
                com.stepup.model.Coupon c1 = new com.stepup.model.Coupon();
                c1.setCode("STEPUP50");
                c1.setDiscountPercentage(50);
                c1.setExpiryDate(java.time.LocalDate.now().plusMonths(3));
                c1.setActive(true);
                couponRepository.save(c1);
                log.info("DataInitializer: Coupon STEPUP50 created");
            }
            if (offerRepository.count() == 0) {
                com.stepup.model.Offer o1 = new com.stepup.model.Offer();
                o1.setTitle("Grand Opening Sale!");
                o1.setDescription(
                        "Celebrate with us! Use code STEPUP50 for a massive 50% discount on all seasonal items.");
                o1.setImage("https://images.unsplash.com/photo-1542291026-7eec264c27ff");
                o1.setExpiryDate(java.time.LocalDate.now().plusMonths(1));
                o1.setActive(true);
                offerRepository.save(o1);

            }
        } catch (Exception e) {
            log.error("DataInitializer: FAILED to seed coupons/offers!", e);
        }

        // 4. Fix null versions
        try {
            jdbcTemplate.execute("UPDATE product SET version = 0 WHERE version IS NULL");
            log.info("DataInitializer: Fixed null versions");
        } catch (Exception e) {
            log.error("DataInitializer: FAILED to fix null versions!", e);
        }

        log.info("=== DataInitializer: Data initialization complete ===");
    }

    private void createProduct(String name, java.math.BigDecimal price, String image, String category, int rating) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setImage(image);
        p.setCategory(category);
        p.setRating(rating);
        p.setDescription("Description for " + name);

        java.util.Map<String, Integer> sizes = new java.util.HashMap<>();
        sizes.put("UK 7", 25);
        sizes.put("UK 8", 25);
        sizes.put("UK 9", 25);
        sizes.put("UK 10", 25);
        p.setSizes(sizes);

        productRepository.save(p);
        log.info("DataInitializer: Product '{}' saved with id={}", name, p.getId());
    }
}
