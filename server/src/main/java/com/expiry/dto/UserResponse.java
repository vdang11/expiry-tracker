package com.expiry.dto;

import java.time.LocalDateTime;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;
}