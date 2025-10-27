package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class ResetPasswordRequestDto {
    private String token;
    private String newPassword;
}