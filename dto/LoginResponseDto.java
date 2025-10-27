package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private String email;
    private Set<String> roles;
}