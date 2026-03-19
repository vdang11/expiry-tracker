package com.example.expiry.controller;

import com.example.expiry.dto.LoginRequest;
import com.example.expiry.dto.SignupRequest;
import com.example.expiry.model.User;
import com.example.expiry.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // SIGNUP
    // =========================
    @PostMapping("/signup")
    public User signup(@RequestBody SignupRequest request) {

        // validate đơn giản
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        // check duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // map DTO → Entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(request.getPassword());

        return userRepository.save(user);
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}