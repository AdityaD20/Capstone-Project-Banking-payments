package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.entity.enums.RequestStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRequestResponseDto {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private BigDecimal amount;
    private RequestStatus status;
    private PaymentType type;
    private String description;
    private String rejectionReason;
    private LocalDateTime createdAt;
}