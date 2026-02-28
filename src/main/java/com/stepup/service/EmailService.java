package com.stepup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOtp(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("StepUp - Your Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
        mailSender.send(message);
    }

    @Async
    public void sendContactEnquiry(String name, String email, String phone, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        // In a real world, this goes to the shop owner's email
        message.setTo("customercarestepup.in@gmail.com"); 
        message.setSubject("New Contact Enquiry: " + name);
        message.setText("Name: " + name + "\nEmail: " + email + "\nPhone: " + phone + "\n\nMessage:\n" + content);
        mailSender.send(message);
    }
}
