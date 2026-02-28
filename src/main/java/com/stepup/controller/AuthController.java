package com.stepup.controller;

import com.stepup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.stepup.dto.UserRegistrationDto;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@Valid @org.springframework.web.bind.annotation.ModelAttribute("registrationDto") UserRegistrationDto registrationDto, 
                           BindingResult result, 
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("registrationDto", registrationDto);
            model.addAttribute("showRegister", true);
            return "login";
        }
        try {
            userService.registerUser(registrationDto.getEmail(), 
                                   registrationDto.getMobileNumber(), 
                                   registrationDto.getPassword(), 
                                   registrationDto.getName());
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error_register", "Registration failed! Email or mobile might already be in use.");
            model.addAttribute("registrationDto", registrationDto);
            model.addAttribute("showRegister", true);
            return "login";
        }
    }
}

