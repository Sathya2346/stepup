package com.stepup.controller;

import com.stepup.model.Cart;
import com.stepup.model.User;
import com.stepup.service.CartService;
import com.stepup.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @org.springframework.beans.factory.annotation.Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Cart cart = cartService.getCart(user);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        model.addAttribute("razorpayKeyId", razorpayKeyId);
        model.addAttribute("activePage", "checkout");
        return "checkout";
    }

    @Autowired
    private com.stepup.service.PaymentService paymentService;

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam String address,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String razorpayOrderId,
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) String razorpaySignature,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            if (address == null || address.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Shipping Address is required.");
                return "redirect:/checkout";
            }
            
            if ("ONLINE".equalsIgnoreCase(paymentMethod)) {
                if (razorpayOrderId == null || paymentId == null || razorpaySignature == null) {
                    redirectAttributes.addFlashAttribute("error", "Payment details missing. Possible spoofing attempt.");
                    return "redirect:/checkout";
                }
                boolean isValid = paymentService.verifySignature(razorpayOrderId, paymentId, razorpaySignature);
                if (!isValid) {
                    redirectAttributes.addFlashAttribute("error", "Payment verification failed. Invalid Signature.");
                    return "redirect:/checkout";
                }
            }
            
            orderService.placeOrder(user, address.trim(), paymentMethod, razorpayOrderId, paymentId);
            return "redirect:/order-confirmation";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order-confirmation")
    public String orderConfirmation(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "order-confirmation";
    }

    @PostMapping("/create-order")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> createOrder(@RequestParam BigDecimal amount, HttpSession session) {
        try {
            String orderId = paymentService.createOrder(amount);
            return org.springframework.http.ResponseEntity.ok(java.util.Collections.singletonMap("id", orderId));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/update-payment")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> updatePayment(@RequestParam String razorpayOrderId,
            @RequestParam String paymentId,
            @RequestParam String status) {
        // In a real app, find the order by razorpayOrderId and update it.
        // For simple flow, we might need to pass our internal order ID too,
        // OR just log it. Since placeOrder is called separately for COD/Online logic
        // split,
        // we might want to consolidate.
        // SIMPLIFICATION: We will keep placeOrder as the final step.
        // Flow: UI calls create-order -> gets ID -> Razorpay -> Success -> UI calls
        // place-order with payment info.
        return org.springframework.http.ResponseEntity.ok("Updated");
    }
}
