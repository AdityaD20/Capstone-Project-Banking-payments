package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class BankAccountDto {
    private String accountNumber;
    private String bankName;
    private String ifscCode;
}