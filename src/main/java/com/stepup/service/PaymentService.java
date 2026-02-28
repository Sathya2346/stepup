package com.stepup.service;

import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PaymentService {

    // You should put these in application.properties
    // For now, hardcoding or using placeholders.
    // Replace with your Actual Keys for testing
    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public String createOrder(BigDecimal amount) throws Exception {
    System.out.println("Creating Razorpay order for amount: " + amount);
    System.out.println("KeyId: " + keyId);

    RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

    JSONObject orderRequest = new JSONObject();
    orderRequest.put("amount", amount.multiply(new BigDecimal("100")).intValue());
    orderRequest.put("currency", "INR");
    orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

    com.razorpay.Order order = razorpay.orders.create(orderRequest);

    System.out.println("Order created: " + order.toString());
    return order.get("id");
}

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return com.razorpay.Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            System.err.println("Signature verification failed: " + e.getMessage());
            return false;
        }
    }

}
