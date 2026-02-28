package com.stepup.controller;

import com.stepup.model.Cart;
import com.stepup.model.User;
import com.stepup.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private com.stepup.repository.CouponRepository couponRepository;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Cart cart = cartService.getCart(user);
        model.addAttribute("cart", cart);
        model.addAttribute("activePage", "cart");
        return "cart";
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String couponCode, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        com.stepup.model.Coupon coupon = couponRepository.findByCode(couponCode.toUpperCase()).orElse(null);
        if (coupon != null && coupon.isActive()) {
            Cart cart = cartService.getCart(user);
            cart.setAppliedCoupon(coupon);
            cartService.saveCart(cart);
            ra.addFlashAttribute("couponSuccess", "Coupon applied: " + coupon.getDiscountPercentage() + "% off!");
        } else {
            ra.addFlashAttribute("couponError", "Invalid or expired coupon code.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(@RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String size,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("User not logged in");
        }
        cartService.addToCart(user, productId, quantity, size);
        return ResponseEntity.ok("Item added to cart");
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> removeFromCart(@RequestParam Long cartItemId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(java.util.Collections.singletonMap("error", "Not logged in"));
        }
        cartService.removeFromCart(cartItemId);
        Cart cart = cartService.getCart(user);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("cartTotal", cart.getTotalAmount());
        result.put("cartSubtotal", cart.getItems().stream().map(i -> i.getSubtotal()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        result.put("isEmpty", cart.getItems().isEmpty());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/update-quantity")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> updateQuantity(@RequestParam Long cartItemId,
            @RequestParam int quantity, HttpSession session) {
        cartService.updateQuantity(cartItemId, quantity);
        User user = (User) session.getAttribute("user");
        Cart cart = cartService.getCart(user);
        com.stepup.model.CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId)).findFirst().orElse(null);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("itemSubtotal", item != null ? item.getSubtotal() : 0);
        result.put("cartTotal", cart.getTotalAmount());
        result.put("cartSubtotal", cart.getItems().stream().map(i -> i.getSubtotal()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/update-size")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> updateSize(@RequestParam Long cartItemId,
            @RequestParam String size, HttpSession session) {
        cartService.updateSize(cartItemId, size);
        User user = (User) session.getAttribute("user");
        Cart cart = cartService.getCart(user);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("cartTotal", cart.getTotalAmount());
        result.put("cartSubtotal", cart.getItems().stream().map(i -> i.getSubtotal()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return ResponseEntity.ok(result);
    }
}
