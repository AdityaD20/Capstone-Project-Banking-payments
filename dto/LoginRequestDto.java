package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
    private String captchaResponse;
}