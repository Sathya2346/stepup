package com.stepup.service;

import com.stepup.model.ChatRule;
import com.stepup.repository.ChatRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRuleService {
    @Autowired
    private ChatRuleRepository chatRuleRepository;

    public List<ChatRule> getAllRules() {
        return chatRuleRepository.findAll();
    }

    public ChatRule getRuleById(Long id) {
        return chatRuleRepository.findById(id).orElse(null);
    }

    public ChatRule saveRule(ChatRule rule) {
        return chatRuleRepository.save(rule);
    }

    public void deleteRule(Long id) {
        chatRuleRepository.deleteById(id);
    }
}
