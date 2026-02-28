package com.stepup.controller;

import com.stepup.model.User;
import com.stepup.repository.UserRepository;
import com.stepup.service.EmailService;
import com.stepup.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/api/auth")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String otp = otpService.generateOtp(email);
            emailService.sendOtp(email, otp);
            return ResponseEntity.ok("OTP sent to your email.");
        }
        return ResponseEntity.badRequest().body("Email not found.");
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (otpService.verifyOtp(email, otp)) {
            return ResponseEntity.ok("OTP verified.");
        }
        return ResponseEntity.badRequest().body("Invalid OTP.");
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword) {
        if (otpService.verifyOtp(email, otp)) {
            User user = userRepository.findByEmail(email).orElseThrow();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            otpService.clearOtp(email);
            return ResponseEntity.ok("Password reset successfully.");
        }
        return ResponseEntity.badRequest().body("OTP verification failed.");
    }
}
