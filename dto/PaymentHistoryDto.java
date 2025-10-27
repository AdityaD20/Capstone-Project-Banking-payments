// src/main/java/com/aurionpro/app/dto/PaymentHistoryDto.java
package com.aurionpro.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aurionpro.app.entity.enums.RequestStatus;

import lombok.Data;

@Data
public class PaymentHistoryDto {
	private Long id;
	private BigDecimal amount;
	private RequestStatus status;
	private String description;
	private LocalDateTime createdAt;
	private String organizationName;
}