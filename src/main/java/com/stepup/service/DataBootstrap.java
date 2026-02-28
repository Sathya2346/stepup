package com.stepup.service;

import com.stepup.model.ChatRule;
import com.stepup.repository.ChatRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataBootstrap implements CommandLineRunner {

    @Autowired
    private ChatRuleRepository chatRuleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (chatRuleRepository.count() == 0) {
            createRule("hello, hi, hey, greetings", "Hello! Welcome to StepUp Support. How can I help you today? 👟 Feel free to ask about your orders, our materials, or shipping policies.", "Track Order, Browse Kids shoes, Payment Help");
            
            createRule("order, track, where, status", "You can track your orders in the 'My Orders' section. Just click on 'Track Order' for any specific purchase to see live updates! We usually deliver within 3-5 business days across India.", "My Orders, Shipping Time?");
            
            createRule("payment, failed, card, upi, razorpay", "StepUp uses encrypted Razorpay gateways. We support UPI, Cards, and Net Banking. If a payment fails, your bank usually reverses the amount in 5-7 working days.", "Payment methods, COD available?");
            
            createRule("size, fit, chart, standard", "We use standard Indian/UK sizing. For toddlers, we recommend checking our 3D size guide or ordering one size up to ensure room for growth.", "Size Chart, Return policy");
            
            createRule("return, exchange, refund, cancel", "StepUp offers a 7-day hassle-free return policy. Items must be unworn and in original packaging. Refunds are processed within 48 hours of quality check completion.", "Start return, Refund status");
            
            createRule("contact, support, help, call, email, human", "Our experts are available from 10 AM to 7 PM. Email: stepup@gmail.com | Phone: +91 9976357250. You can also use our Enquiry Form on the Contact Us page.", "Contact Form");

            createRule("material, quality, leather, synthetic, pure", "We take pride in our quality. Our premium sports range uses breathable synthetic mesh, while our formal collection features 100% genuine Italian leather. All shoes are hand-inspected for durability.", "Quality Guarantees, Material specific");

            createRule("shipping, time, delay, delhi, mumbai, kerala", "We ship Pan-India. Metro cities (Delhi, Mumbai, B'lore) take 2-3 days. Other regions take 4-6 days. We'll send you a WhatsApp tracking link as soon as your order leaves our warehouse!", "Express Shipping?");

            createRule("discount, offer, coupon, promo", "New users get 50% off on their first order! Use code 'STEPUP50' at checkout. Keep an eye on our 'Offers' section for seasonal clearance sales.", "Apply Coupon");
        }
    }

    private void createRule(String keywords, String response, String suggestions) {
        ChatRule rule = new ChatRule();
        rule.setKeywords(keywords);
        rule.setResponse(response);
        rule.setSuggestions(suggestions);
        chatRuleRepository.save(rule);
    }
}
