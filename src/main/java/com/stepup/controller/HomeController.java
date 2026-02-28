package com.stepup.controller;

import com.stepup.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.stepup.repository.OfferRepository offerRepository;

    @Autowired
    private com.stepup.repository.ContactMessageRepository contactRepository;

    @Autowired
    private com.stepup.service.EmailService emailService;

    @PostMapping("/contact/submit")
    public String submitContact(@RequestParam String name, @RequestParam String email,
            @RequestParam String phone, @RequestParam String message, Model model) {
        com.stepup.model.ContactMessage msg = new com.stepup.model.ContactMessage();
        msg.setName(name);
        msg.setEmail(email);
        msg.setPhone(phone);
        msg.setMessage(message);
        contactRepository.save(msg);

        // Send email notification to Admin
        try {
            emailService.sendContactEnquiry(name, email, phone, message);
        } catch (Exception e) {
            e.printStackTrace(); // Log error but don't fail the request
        }

        return "redirect:/contact?success";
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model) {
        boolean searchMode = keyword != null || category != null || minPrice != null || maxPrice != null;
        if (searchMode) {
            model.addAttribute("products", productService.searchProducts(keyword, category, minPrice, maxPrice,
                    org.springframework.data.domain.Sort.unsorted()));
        } else {
            java.util.List<com.stepup.model.Product> allProducts = productService.getAllProducts();
            allProducts.sort((a, b) -> b.getId().compareTo(a.getId()));
            model.addAttribute("products", allProducts.subList(0, Math.min(4, allProducts.size())));
        }
        model.addAttribute("searchMode", searchMode);
        model.addAttribute("activePage", "home");
        return "index";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            Model model) {
        org.springframework.data.domain.Sort sortOrder = org.springframework.data.domain.Sort.unsorted();
        if ("Price:Low-High".equals(sort)) {
            sortOrder = org.springframework.data.domain.Sort.by("price").ascending();
        } else if ("Price:High-Low".equals(sort)) {
            sortOrder = org.springframework.data.domain.Sort.by("price").descending();
        } else if ("New Arrivals".equals(sort) || "Newest".equals(sort)) {
            sortOrder = org.springframework.data.domain.Sort.by("id").descending();
        } else if ("A-Z".equals(sort)) {
            sortOrder = org.springframework.data.domain.Sort.by("name").ascending();
        } else if ("Z-A".equals(sort)) {
            sortOrder = org.springframework.data.domain.Sort.by("name").descending();
        } else if ("Top Rated".equals(sort)) {
            sortOrder = org.springframework.data.domain.Sort.by("rating").descending();
        }

        model.addAttribute("products", productService.searchProducts(keyword, category, minPrice, maxPrice, sortOrder));
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSort", sort);
        model.addAttribute("activePage", "products");
        return "products";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activePage", "contact");
        return "contact";
    }

    @GetMapping("/offers")
    public String offers(Model model) {
        model.addAttribute("offers", offerRepository.findByActiveTrue());
        model.addAttribute("activePage", "offers");
        return "offers";
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new com.stepup.dto.UserRegistrationDto());
        }
        return "login";
    }
}
