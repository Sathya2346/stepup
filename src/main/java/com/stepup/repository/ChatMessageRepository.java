package com.stepup.repository;

import com.stepup.model.ChatMessage;
import com.stepup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByTimestampAsc(User user);
}
