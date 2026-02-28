package com.stepup.config;

import com.stepup.model.User;
import com.stepup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.stepup.model.ChatMessage;
import com.stepup.repository.ChatMessageRepository;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Autowired
    private ChatLogoutHandler chatLogoutHandler;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/product/**", "/cart/add", "/products", "/offers", "/contact/**",
                                "/login", "/register", "/error", "/favicon.ico",
                                "/api/auth/**", "/api/chat/**", "/style.css", "/script.js", "/*.css", "/*.js",
                                "/images/**", "/uploads/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .successHandler(loginSuccessHandler())
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oauth2SuccessHandler()))
                .logout(logout -> logout
                        .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout", "GET"))
                        .addLogoutHandler(chatLogoutHandler)
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            String identifier = authentication.getName();
            User user = userRepository.findByIdentifier(identifier).orElseThrow();
            request.getSession().setAttribute("user", user);

            List<ChatMessage> chatHistory = chatMessageRepository.findByUserOrderByTimestampAsc(user);
            request.getSession().setAttribute("chatHistory", chatHistory);

            if (user.getRole() == User.Role.ADMIN) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setProfilePicture(picture);
                newUser.setPassword("OAUTH2_USER");
                newUser.setRole(User.Role.USER);
                return userRepository.save(newUser);
            });

            // Update picture if it changed
            if (picture != null && !picture.equals(user.getProfilePicture())) {
                user.setProfilePicture(picture);
                userRepository.save(user);
            }

            request.getSession().setAttribute("user", user);

            List<ChatMessage> chatHistory = chatMessageRepository.findByUserOrderByTimestampAsc(user);
            request.getSession().setAttribute("chatHistory", chatHistory);

            response.sendRedirect("/");
        };
    }
}
