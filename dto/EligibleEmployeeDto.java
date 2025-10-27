package com.aurionpro.app.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibleEmployeeDto {
	private Long employeeId;
	private String employeeNumber;
	private String fullName;
	private BigDecimal defaultNetSalary;
}