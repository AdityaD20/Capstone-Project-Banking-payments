package com.aurionpro.app.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.EligibleEmployeeDto;
import com.aurionpro.app.dto.EmployeeCreateRequestDto;
import com.aurionpro.app.dto.EmployeeFinalizeRequestDto;
import com.aurionpro.app.dto.EmployeeResponseDto;
import com.aurionpro.app.dto.PaySlipSummaryDto;
import com.aurionpro.app.dto.UpdateBankAccountDto;
import com.aurionpro.app.dto.UpdateSalaryStructureDto;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletResponse;

public interface EmployeeService {

	EmployeeResponseDto addEmployee(EmployeeCreateRequestDto createDto);

	EmployeeResponseDto submitDocuments(String employeeEmail, List<MultipartFile> files) throws IOException;

	EmployeeResponseDto activateEmployee(Long employeeId, EmployeeFinalizeRequestDto finalizeDto);

	EmployeeResponseDto rejectDocuments(Long employeeId, String reason);

	List<EmployeeResponseDto> getAllEmployeesForCurrentOrganization();

	void softDeleteEmployee(Long employeeId);
	
	EmployeeResponseDto enableEmployee(Long employeeId);

	Page<PaySlipSummaryDto> getMyPaySlipHistory(Pageable pageable);

	void downloadMyPaySlip(Long paySlipId, HttpServletResponse response) throws DocumentException, IOException;

	Page<EligibleEmployeeDto> getEligibleEmployeesForPayroll(Pageable pageable);

	EmployeeResponseDto getEmployeeById(Long employeeId);

	EmployeeResponseDto updateBankAccount(Long employeeId, UpdateBankAccountDto dto);

	EmployeeResponseDto updateSalaryStructure(Long employeeId, UpdateSalaryStructureDto dto);
	
	EmployeeResponseDto getMyEmployeeDetails(Authentication authentication);
}