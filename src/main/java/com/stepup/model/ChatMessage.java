package com.stepup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String sender; // "User" or "Bot"

    @Column(length = 4000)
    private String content;

    private LocalDateTime timestamp = LocalDateTime.now();

    public ChatMessage() {
    }

    public ChatMessage(User user, String sender, String content) {
        this.user = user;
        this.sender = sender;
        this.content = content;
    }
}
