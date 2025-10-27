package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.OrganizationStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrganizationDto {
    private Long id;
    private String name;
    private OrganizationStatus status;
    private BigDecimal balance;
    private String email; 
    private LocalDateTime createdAt;
    private String rejectionReason;
}