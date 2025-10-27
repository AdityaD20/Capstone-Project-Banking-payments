package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Data
public class PaySlipSummaryDto {
    private Long id;
    private YearMonth payPeriod;
    private BigDecimal netSalary;
    private LocalDateTime createdAt;
}