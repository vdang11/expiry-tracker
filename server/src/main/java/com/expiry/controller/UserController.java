package com.expiry.controller;

import com.expiry.dto.LoginRequest;
import com.expiry.dto.LoginResponse;
import com.expiry.dto.SignupRequest;
import com.expiry.dto.UserResponse;
import com.expiry.entity.User;
import com.expiry.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public UserResponse signup(@RequestBody SignupRequest request) {

        User user = userService.signup(
                request.getEmail(),
                request.getName(),
                request.getPassword()
        );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        return userService.login(
                request.getEmail(),
                request.getPassword()
        );
    }
}