package com.stepup.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.validation.constraints.Email(message = "Invalid email format")
    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @jakarta.validation.constraints.NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    private String name;
    
    @Column(unique = true)
    private String mobileNumber;

    private String address;
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        USER, ADMIN
    }
}
