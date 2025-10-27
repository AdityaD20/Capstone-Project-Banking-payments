package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.BankAccountDto;
import com.aurionpro.app.dto.EmployeeFinalizeRequestDto;
import com.aurionpro.app.dto.EmployeeResponseDto;
import com.aurionpro.app.dto.SalaryStructureDto;
import com.aurionpro.app.entity.BankAccountDetails;
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.SalaryStructure;
import com.aurionpro.app.entity.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public EmployeeResponseDto toDto(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        EmployeeResponseDto employeeResponseDto = new EmployeeResponseDto();

        employeeResponseDto.setBankAccount( bankAccountDetailsToBankAccountDto( employee.getBankAccount() ) );
        employeeResponseDto.setSalaryStructure( salaryStructureToSalaryStructureDto( employee.getSalaryStructure() ) );
        employeeResponseDto.setId( employee.getId() );
        employeeResponseDto.setFirstName( employee.getFirstName() );
        employeeResponseDto.setLastName( employee.getLastName() );
        employeeResponseDto.setEmail( employeeUserEmail( employee ) );
        employeeResponseDto.setStatus( employee.getStatus() );
        employeeResponseDto.setRejectionReason( employee.getRejectionReason() );
        employeeResponseDto.setEmployeeNumber( employee.getEmployeeNumber() );

        return employeeResponseDto;
    }

    @Override
    public BankAccountDetails finalizeDtoToBankAccount(EmployeeFinalizeRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        BankAccountDetails bankAccountDetails = new BankAccountDetails();

        bankAccountDetails.setAccountNumber( dto.getAccountNumber() );
        bankAccountDetails.setIfscCode( dto.getIfscCode() );
        bankAccountDetails.setBankName( dto.getBankName() );

        return bankAccountDetails;
    }

    @Override
    public SalaryStructure finalizeDtoToSalaryStructure(EmployeeFinalizeRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        SalaryStructure salaryStructure = new SalaryStructure();

        salaryStructure.setBasicSalary( dto.getBasicSalary() );
        salaryStructure.setHra( dto.getHra() );
        salaryStructure.setDa( dto.getDa() );
        salaryStructure.setPf( dto.getPf() );
        salaryStructure.setOtherAllowances( dto.getOtherAllowances() );

        return salaryStructure;
    }

    protected BankAccountDto bankAccountDetailsToBankAccountDto(BankAccountDetails bankAccountDetails) {
        if ( bankAccountDetails == null ) {
            return null;
        }

        BankAccountDto bankAccountDto = new BankAccountDto();

        bankAccountDto.setAccountNumber( bankAccountDetails.getAccountNumber() );
        bankAccountDto.setBankName( bankAccountDetails.getBankName() );
        bankAccountDto.setIfscCode( bankAccountDetails.getIfscCode() );

        return bankAccountDto;
    }

    protected SalaryStructureDto salaryStructureToSalaryStructureDto(SalaryStructure salaryStructure) {
        if ( salaryStructure == null ) {
            return null;
        }

        SalaryStructureDto salaryStructureDto = new SalaryStructureDto();

        salaryStructureDto.setBasicSalary( salaryStructure.getBasicSalary() );
        salaryStructureDto.setHra( salaryStructure.getHra() );
        salaryStructureDto.setDa( salaryStructure.getDa() );
        salaryStructureDto.setPf( salaryStructure.getPf() );
        salaryStructureDto.setOtherAllowances( salaryStructure.getOtherAllowances() );

        return salaryStructureDto;
    }

    private String employeeUserEmail(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        User user = employee.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
