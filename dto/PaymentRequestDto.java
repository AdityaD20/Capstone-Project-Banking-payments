package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    private Long vendorId;
    private BigDecimal amount;
    private String description;
}