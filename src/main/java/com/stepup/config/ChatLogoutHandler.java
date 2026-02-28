package com.stepup.config;

import com.stepup.model.ChatMessage;
import com.stepup.repository.ChatMessageRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatLogoutHandler implements LogoutHandler {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            List<ChatMessage> chatHistory = (List<ChatMessage>) session.getAttribute("chatHistory");
            if (chatHistory != null && !chatHistory.isEmpty()) {
                chatMessageRepository.saveAll(chatHistory);
                session.removeAttribute("chatHistory");
            }
        }
    }
}
