package com.aurionpro.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.aurionpro.app.dto.EmployeeFinalizeRequestDto;
import com.aurionpro.app.dto.EmployeeResponseDto;
import com.aurionpro.app.entity.BankAccountDetails;
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.SalaryStructure;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "firstName", target = "firstName"),
        @Mapping(source = "lastName", target = "lastName"),
        @Mapping(source = "user.email", target = "email"),
        @Mapping(source = "status", target = "status"),
        @Mapping(source = "bankAccount.accountNumber", target = "bankAccount.accountNumber"),
        @Mapping(source = "bankAccount.bankName", target = "bankAccount.bankName"),
        @Mapping(source = "bankAccount.ifscCode", target = "bankAccount.ifscCode"),
        @Mapping(source = "salaryStructure.basicSalary", target = "salaryStructure.basicSalary"),
        @Mapping(source = "salaryStructure.hra", target = "salaryStructure.hra"),
        @Mapping(source = "salaryStructure.da", target = "salaryStructure.da"),
        @Mapping(source = "salaryStructure.pf", target = "salaryStructure.pf"),
        @Mapping(source = "salaryStructure.otherAllowances", target = "salaryStructure.otherAllowances"),
        @Mapping(source = "rejectionReason", target = "rejectionReason")
    })
    EmployeeResponseDto toDto(Employee employee);

    @Mapping(source = "accountNumber", target = "accountNumber")
    @Mapping(source = "ifscCode", target = "ifscCode")
    @Mapping(source = "bankName", target = "bankName")
    BankAccountDetails finalizeDtoToBankAccount(EmployeeFinalizeRequestDto dto);

    @Mapping(source = "basicSalary", target = "basicSalary")
    @Mapping(source = "hra", target = "hra")
    @Mapping(source = "da", target = "da")
    @Mapping(source = "pf", target = "pf")
    @Mapping(source = "otherAllowances", target = "otherAllowances")
    SalaryStructure finalizeDtoToSalaryStructure(EmployeeFinalizeRequestDto dto);
}