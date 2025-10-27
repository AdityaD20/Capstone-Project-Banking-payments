package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinalApprovalRequestDto {
	private String accountNumber;
    private String ifscCode;
    private String bankName;
    private BigDecimal initialBalance;
}