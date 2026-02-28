package com.stepup.controller;

import com.stepup.model.Order;
import com.stepup.model.Product;
import com.stepup.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @org.springframework.beans.factory.annotation.Value("${admin.notification.phone}")
    private String adminPhone;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.stepup.repository.OrderRepository orderRepository;

    @Autowired
    private com.stepup.service.NotificationService notificationService;

    @Autowired
    private com.stepup.service.ChatRuleService chatRuleService;

    @Autowired
    private com.stepup.repository.CouponRepository couponRepository;

    @Autowired
    private com.stepup.repository.OfferRepository offerRepository;

    @GetMapping("/chatbot")
    public String manageChatbot(Model model) {
        model.addAttribute("rules", chatRuleService.getAllRules());
        model.addAttribute("activePage", "chatbot");
        return "admin/chatbot";
    }

    @GetMapping("/chatbot/new")
    public String showChatbotForm(Model model) {
        model.addAttribute("rule", new com.stepup.model.ChatRule());
        model.addAttribute("activePage", "chatbot");
        return "admin/chatbot_form";
    }

    @PostMapping("/chatbot/save")
    public String saveChatbotRule(@ModelAttribute("rule") com.stepup.model.ChatRule rule) {
        chatRuleService.saveRule(rule);
        return "redirect:/admin/chatbot";
    }

    @GetMapping("/chatbot/edit/{id}")
    public String editChatbotRule(@PathVariable Long id, Model model) {
        model.addAttribute("rule", chatRuleService.getRuleById(id));
        model.addAttribute("activePage", "chatbot");
        return "admin/chatbot_form";
    }

    @GetMapping("/coupons")
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponRepository.findAll());
        model.addAttribute("activePage", "coupons");
        return "admin/coupons";
    }

    @GetMapping("/coupon/new")
    public String newCoupon(Model model) {
        model.addAttribute("coupon", new com.stepup.model.Coupon());
        model.addAttribute("activePage", "coupons");
        return "admin/coupon_form";
    }

    @PostMapping("/coupon/save")
    public String saveCoupon(@ModelAttribute("coupon") com.stepup.model.Coupon coupon) {
        couponRepository.save(coupon);
        return "redirect:/admin/coupons";
    }

    @GetMapping("/coupon/edit/{id}")
    public String editCoupon(@PathVariable Long id, Model model) {
        model.addAttribute("coupon", couponRepository.findById(id).orElse(null));
        model.addAttribute("activePage", "coupons");
        return "admin/coupon_form";
    }

    @GetMapping("/coupon/delete/{id}")
    public String deleteCoupon(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return "redirect:/admin/coupons";
    }

    @GetMapping("/offers")
    public String listAdminOffers(Model model) {
        model.addAttribute("offers", offerRepository.findAll());
        model.addAttribute("activePage", "offers");
        return "admin/offers";
    }

    @GetMapping("/offer/new")
    public String newOffer(Model model) {
        model.addAttribute("offer", new com.stepup.model.Offer());
        model.addAttribute("activePage", "offers");
        return "admin/offer_form";
    }

    @PostMapping("/offer/save")
    public String saveOffer(@ModelAttribute("offer") com.stepup.model.Offer offer) {
        offerRepository.save(offer);
        return "redirect:/admin/offers";
    }

    @GetMapping("/offer/edit/{id}")
    public String editOffer(@PathVariable Long id, Model model) {
        model.addAttribute("offer", offerRepository.findById(id).orElse(null));
        model.addAttribute("activePage", "offers");
        return "admin/offer_form";
    }

    @GetMapping("/offer/delete/{id}")
    public String deleteOffer(@PathVariable Long id) {
        offerRepository.deleteById(id);
        return "redirect:/admin/offers";
    }

    @GetMapping
    public String dashboard(Model model) {
        // Analytics
        java.util.List<Order> allOrders = orderRepository.findAll();
        java.math.BigDecimal totalSales = allOrders.stream().map(Order::getTotalAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        long totalOrders = allOrders.size();
        long totalProducts = productService.getAllProducts().size(); // Or add count method in service

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("recentOrders", allOrders.stream()
                .sorted(java.util.Comparator.comparing(Order::getOrderDate).reversed())
                .limit(5)
                .collect(java.util.stream.Collectors.toList()));

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/product/new")
    public String showProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("activePage", "products");
        return "admin/product_form";
    }

    @PostMapping("/product/save")
    public String saveProduct(@jakarta.validation.Valid @ModelAttribute("product") Product product,
                              org.springframework.validation.BindingResult result,
                              @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
                              Model model) throws java.io.IOException {
        
        if (result.hasErrors()) {
            model.addAttribute("activePage", "products");
            return "admin/product_form";
        }

        // Compute inStock based on total quantity if user didn't manually toggle it
        if (product.getTotalStock() > 0) {
            product.setInStock(true);
        } else {
            product.setInStock(false);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = org.springframework.util.StringUtils.cleanPath(imageFile.getOriginalFilename());
            String uniqueFileName = java.util.UUID.randomUUID().toString() + "_" + fileName;
            String uploadDir = "uploads/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            try (java.io.InputStream inputStream = imageFile.getInputStream()) {
                java.nio.file.Path filePath = uploadPath.resolve(uniqueFileName);
                java.nio.file.Files.copy(inputStream, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                product.setImage("/uploads/" + uniqueFileName);
            } catch (java.io.IOException e) {
                throw new java.io.IOException("Could not save image file: " + fileName, e);
            }
        }

        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("activePage", "products");
        return "admin/products";
    }

    @GetMapping("/product/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("activePage", "products");
        return "admin/product_form";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("activePage", "orders");
        return "admin/orders";
    }

    @PostMapping("/order/status/{id}")
    public String updateOrderStatus(@PathVariable Long id, 
                                    @RequestParam Order.OrderStatus status,
                                    @RequestParam(required = false) Double latitude,
                                    @RequestParam(required = false) Double longitude,
                                    @RequestParam(required = false) java.time.LocalDate expectedDate) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status);
            if (latitude != null) order.setCurrentLatitude(latitude);
            if (longitude != null) order.setCurrentLongitude(longitude);
            if (expectedDate != null) order.setExpectedDeliveryDate(expectedDate);
            
            orderRepository.save(order);
            
            // Send WhatsApp Notification
            String message = "Hello " + (order.getUser() != null ? order.getUser().getName() : "Customer") + 
                             ", your order #" + order.getId() + " is now " + status + ". Tracking: " + order.getTrackingNumber();
            String recipient = (order.getUser() != null && order.getUser().getMobileNumber() != null) ? 
                               order.getUser().getMobileNumber() : adminPhone;
            notificationService.sendWhatsApp(recipient, message);
        }
        return "redirect:/admin/orders";
    }
}
