package com.stepup.controller;

import com.stepup.model.User;
import com.stepup.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @ModelAttribute("cartQuantity")
    public Integer getCartQuantity(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            return cartService.getCartQuantity(user);
        } catch (Exception e) {
            return 0;
        }
    }
}
