package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private String oldPassword;
    private String newPassword;
}