package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequestDto {
    private BigDecimal amount;
    private String description;
}