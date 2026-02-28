package com.stepup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Order order;

    private String reason;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    private LocalDateTime requestedAt = LocalDateTime.now();

    public enum ReturnStatus {
        PENDING, APPROVED, REJECTED, COMPLETED
    }
}
