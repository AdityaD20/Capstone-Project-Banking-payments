package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeFinalizeRequestDto {
	 private String employeeNumber;
	 private Long departmentId;
	
	// Bank account details
    private String accountNumber;
    private String ifscCode;
    private String bankName;
    
    // Salary structure details
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal pf;
    private BigDecimal otherAllowances;
}