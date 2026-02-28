package com.stepup.controller;

import com.stepup.model.Order;
import com.stepup.model.User;
import com.stepup.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String myOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);
        model.addAttribute("activePage", "orders");
        return "my-orders";
    }

    @GetMapping("/track/{id}")
    public String trackOrder(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Order order = orderService.getOrderById(id);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("activePage", "orders");
        return "track-order";
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam Long orderId, @RequestParam String reason, HttpSession session,
            RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        try {
            orderService.cancelOrder(orderId, reason);
            ra.addFlashAttribute("successMessage", "Order #" + orderId + " has been cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/return")
    public String returnOrder(@RequestParam Long orderId, @RequestParam String reason, HttpSession session,
            RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        try {
            orderService.requestReturn(orderId, reason);
            ra.addFlashAttribute("successMessage", "Return request submitted for Order #" + orderId + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }
}
