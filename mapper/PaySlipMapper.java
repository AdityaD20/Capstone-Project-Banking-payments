package com.aurionpro.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.aurionpro.app.dto.PaySlipDetailDto;
import com.aurionpro.app.dto.PaySlipSummaryDto;
import com.aurionpro.app.entity.PaySlip;
import com.aurionpro.app.service.PayrollService;

@Mapper(componentModel = "spring", uses = {PayrollService.class})
public interface PaySlipMapper {
	
	PaySlipSummaryDto toSummaryDto(PaySlip paySlip);

    @Mappings({
        @Mapping(source = "employee.organization.name", target = "organizationName"),
        @Mapping(target = "employeeName", expression = "java(paySlip.getEmployee().getFirstName() + \" \" + paySlip.getEmployee().getLastName())"),
        @Mapping(source = "employee.employeeNumber", target = "employeeNumber"),
        @Mapping(source = "basicSalary", target = "basicSalary"),
        @Mapping(source = "hra", target = "hra"),
        @Mapping(source = "dearnessAllowance", target = "da"),
        @Mapping(source = "providentFund", target = "pf"),
        @Mapping(source = "otherAllowances", target = "otherAllowances"),
        @Mapping(source = "netSalary", target = "netSalary"),
        
        @Mapping(source = "paySlip", target = "totalEarnings", qualifiedByName = "calculateTotalEarnings"),
        @Mapping(source = "paySlip", target = "totalDeductions", qualifiedByName = "calculateTotalDeductions")
    })
    PaySlipDetailDto toPaySlipDetailDto(PaySlip paySlip);
    
}