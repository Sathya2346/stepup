package com.stepup.service;

import com.stepup.model.User;
import com.stepup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public User registerUser(String email, String mobileNumber, String password, String name) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByMobileNumber(mobileNumber).isPresent()) {
            throw new RuntimeException("Mobile number already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setMobileNumber(mobileNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }
}
