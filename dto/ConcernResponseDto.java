package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.ConcernStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConcernResponseDto {
    private Long id;
    private String description;
    private ConcernStatus status;
    private LocalDateTime createdAt;
    private String employeeName; // For organization view
}