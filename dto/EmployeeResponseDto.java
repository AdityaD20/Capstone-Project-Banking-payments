package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.EmployeeStatus;
import lombok.Data;

@Data
public class EmployeeResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeNumber;
    private EmployeeStatus status;
    private BankAccountDto bankAccount;
    private SalaryStructureDto salaryStructure;
    private String rejectionReason;
}