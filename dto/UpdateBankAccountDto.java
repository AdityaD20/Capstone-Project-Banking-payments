package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class UpdateBankAccountDto {
    private String accountNumber;
    private String ifscCode;
    private String bankName;
}