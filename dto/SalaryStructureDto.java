package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalaryStructureDto {
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal pf;
    private BigDecimal otherAllowances;
}