package com.expiry.service;

import com.expiry.entity.User;
import com.expiry.repository.UserRepository;
import com.expiry.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User signup(String email, String name, String rawPassword) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // HASH PASSWORD
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setEmail(email.trim());
        user.setName(name);
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    public String login(String email, String rawPassword) {

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // SO PASSWORD
        boolean isMatch = passwordEncoder.matches(rawPassword, user.getPassword());

        if (!isMatch) {
            throw new IllegalArgumentException("Invalid password");
        }

        // TRẢ JWT
        return jwtService.generateToken(user.getId(), user.getEmail());
    }
}