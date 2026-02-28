package com.stepup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private BigDecimal totalAmount;

    // Discount Tracking
    private BigDecimal discountAmount;
    private String couponCode;

    @Column(unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;

    private LocalDate expectedDeliveryDate;
    private Double currentLatitude;
    private Double currentLongitude;

    private String shippingAddress;

    private String razorpayOrderId;
    private String paymentId;
    private String paymentStatus; // PENDING, PAID, FAILED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    public enum OrderStatus {
        PENDING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURN_REQUESTED, RETURNED
    }

    private String cancelReason;
    private String returnReason;
    private String refundStatus; // NOT_APPLICABLE, PENDING, PROCESSED
}
