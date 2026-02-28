package com.stepup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @ManyToOne
    private Coupon appliedCoupon;

    public BigDecimal getTotalAmount() {
        BigDecimal subtotal = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (appliedCoupon != null && appliedCoupon.isActive()) {
            BigDecimal discountPercent = BigDecimal.valueOf(appliedCoupon.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
            BigDecimal discount = subtotal.multiply(discountPercent);
            return subtotal.subtract(discount);
        }
        return subtotal;
    }

    public Integer getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
