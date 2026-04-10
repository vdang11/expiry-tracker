package com.expiry.dto;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SignupRequest {
    private String email;
    private String name;
    private String password;
}