package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.PaySlipDetailDto;
import com.aurionpro.app.dto.PaySlipSummaryDto;
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.PaySlip;
import com.aurionpro.app.service.PayrollService;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class PaySlipMapperImpl implements PaySlipMapper {

    @Autowired
    private PayrollService payrollService;

    @Override
    public PaySlipSummaryDto toSummaryDto(PaySlip paySlip) {
        if ( paySlip == null ) {
            return null;
        }

        PaySlipSummaryDto paySlipSummaryDto = new PaySlipSummaryDto();

        paySlipSummaryDto.setCreatedAt( paySlip.getCreatedAt() );
        paySlipSummaryDto.setId( paySlip.getId() );
        paySlipSummaryDto.setNetSalary( paySlip.getNetSalary() );
        paySlipSummaryDto.setPayPeriod( paySlip.getPayPeriod() );

        return paySlipSummaryDto;
    }

    @Override
    public PaySlipDetailDto toPaySlipDetailDto(PaySlip paySlip) {
        if ( paySlip == null ) {
            return null;
        }

        PaySlipDetailDto.PaySlipDetailDtoBuilder paySlipDetailDto = PaySlipDetailDto.builder();

        paySlipDetailDto.organizationName( paySlipEmployeeOrganizationName( paySlip ) );
        paySlipDetailDto.employeeNumber( paySlipEmployeeEmployeeNumber( paySlip ) );
        paySlipDetailDto.basicSalary( paySlip.getBasicSalary() );
        paySlipDetailDto.hra( paySlip.getHra() );
        paySlipDetailDto.da( paySlip.getDearnessAllowance() );
        paySlipDetailDto.pf( paySlip.getProvidentFund() );
        paySlipDetailDto.otherAllowances( paySlip.getOtherAllowances() );
        paySlipDetailDto.netSalary( paySlip.getNetSalary() );
        paySlipDetailDto.totalEarnings( payrollService.calculateTotalEarnings( paySlip ) );
        paySlipDetailDto.totalDeductions( payrollService.calculateTotalDeductions( paySlip ) );
        paySlipDetailDto.payPeriod( paySlip.getPayPeriod() );

        paySlipDetailDto.employeeName( paySlip.getEmployee().getFirstName() + " " + paySlip.getEmployee().getLastName() );

        return paySlipDetailDto.build();
    }

    private String paySlipEmployeeOrganizationName(PaySlip paySlip) {
        if ( paySlip == null ) {
            return null;
        }
        Employee employee = paySlip.getEmployee();
        if ( employee == null ) {
            return null;
        }
        Organization organization = employee.getOrganization();
        if ( organization == null ) {
            return null;
        }
        String name = organization.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String paySlipEmployeeEmployeeNumber(PaySlip paySlip) {
        if ( paySlip == null ) {
            return null;
        }
        Employee employee = paySlip.getEmployee();
        if ( employee == null ) {
            return null;
        }
        String employeeNumber = employee.getEmployeeNumber();
        if ( employeeNumber == null ) {
            return null;
        }
        return employeeNumber;
    }
}
