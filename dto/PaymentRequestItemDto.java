package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestItemDto {
    private Long employeeId;
    private BigDecimal amount;
}