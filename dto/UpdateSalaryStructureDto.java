package com.aurionpro.app.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateSalaryStructureDto {
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal pf;
    private BigDecimal otherAllowances;
}