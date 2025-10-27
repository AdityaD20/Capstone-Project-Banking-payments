package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class OrganizationRegistrationRequestDto {
    private String organizationName;
    private String email;
    private String password;
}