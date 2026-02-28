package com.stepup.controller;

import com.stepup.model.Product;
import com.stepup.model.Review;
import com.stepup.model.User;
import com.stepup.repository.ReviewRepository;
import com.stepup.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/";
        }
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewRepository.findByProductOrderByDateDesc(product));
        return "product-details";
    }

    @PostMapping("/{id}/review")
    public String addReview(@PathVariable Long id, @RequestParam int rating, @RequestParam String comment, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Product product = productService.getProductById(id);
        if (product != null) {
            Review review = new Review();
            review.setProduct(product);
            review.setUser(user);
            review.setRating(rating);
            review.setComment(comment);
            review.setDate(LocalDateTime.now());
            reviewRepository.save(review);
            
            // Recalculate average rating for product (optional but good)
            updateProductRating(product);
        }
        return "redirect:/product/" + id;
    }

    private void updateProductRating(Product product) {
        // Simple mock calc or real implementation
        // For simplicity, just letting new reviews pile up.
        // A real app would sum/count and update product.rating
    }
}
