// src/main/java/com/aurionpro/app/dto/VendorCreateRequestDto.java
package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class VendorCreateRequestDto {
    // User Details
    private String name;
    private String email;
    private String phone;
    
    // Bank Account Details
    private String accountNumber;
    private String ifscCode;
    private String bankName;
}