package com.aurionpro.app.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // Using builder pattern is nice for creating complex DTOs
public class PaySlipDetailDto {
    // Company & Employee Info
    private String organizationName;
    private String employeeName;
    private String employeeNumber;
    private String departmentName;
    private YearMonth payPeriod;

    // Earnings
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal otherAllowances;
    private BigDecimal totalEarnings;

    // Deductions
    private BigDecimal pf;
    private BigDecimal totalDeductions;

    // Final Amount
    private BigDecimal netSalary;
}