package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.RoleType;
import lombok.Data;

@Data 
public class RegistrationRequestDto {
    private String email;
    private String password;
    private RoleType role;
    private String captchaResponse;
}