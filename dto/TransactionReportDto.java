package com.aurionpro.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.entity.enums.RequestStatus;

import lombok.Data;

@Data
public class TransactionReportDto {
	private Long transactionId;
	private PaymentType type;
	private RequestStatus status;
	private BigDecimal amount;
	private String description;
	private String recipientName;
	private LocalDateTime transactionDate;
}