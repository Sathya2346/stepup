package com.stepup.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ChatRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keywords; // Comma separated keywords

    @Column(nullable = false, length = 1000)
    private String response;

    @Column(length = 500)
    private String suggestions; // Comma separated suggestions
}
