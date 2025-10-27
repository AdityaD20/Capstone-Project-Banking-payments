package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.RequestStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepositResponseDto {
    private Long id;
    private String organizationName;
    private BigDecimal amount;
    private RequestStatus status;
    private String description;
    private String rejectionReason;
    private LocalDateTime createdAt;
}