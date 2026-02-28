package com.stepup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @jakarta.validation.constraints.NotBlank(message = "Product name is required")
    private String name;
    private String description;
    
    @jakarta.validation.constraints.NotNull(message = "Price is required")
    @jakarta.validation.constraints.Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price; // MRP
    private String image; // URL
    private String category; // e.g., Kids, Womens, Mens
    private String color;
    
    @jakarta.validation.constraints.Min(value = 1, message = "Rating must be at least 1")
    @jakarta.validation.constraints.Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating; // 1-5

    @Column(columnDefinition = "boolean default false")
    private boolean isFeatured;

    @Column(columnDefinition = "boolean default true")
    private boolean inStock;

    @ElementCollection
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "size_name")
    @Column(name = "quantity")
    private Map<String, Integer> sizes = new HashMap<>();

    public Integer getTotalStock() {
        return sizes.values().stream().mapToInt(Integer::intValue).sum();
    }
}
