package com.aurionpro.app.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.EligibleEmployeeDto;
import com.aurionpro.app.dto.EmployeeCreateRequestDto;
import com.aurionpro.app.dto.EmployeeFinalizeRequestDto;
import com.aurionpro.app.dto.EmployeeResponseDto;
import com.aurionpro.app.dto.PaySlipDetailDto;
import com.aurionpro.app.dto.PaySlipSummaryDto;
import com.aurionpro.app.dto.UpdateBankAccountDto;
import com.aurionpro.app.dto.UpdateSalaryStructureDto;
import com.aurionpro.app.entity.BankAccountDetails;
import com.aurionpro.app.entity.Document; // IMPORT Document
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.PaySlip;
import com.aurionpro.app.entity.SalaryStructure;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.enums.EmployeeStatus;
import com.aurionpro.app.entity.enums.OrganizationStatus;
import com.aurionpro.app.entity.enums.RoleType;
import com.aurionpro.app.entity.user.Role;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.DuplicateResourceException;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.EmployeeMapper;
import com.aurionpro.app.mapper.PaySlipMapper;
import com.aurionpro.app.repository.DocumentRepository; // IMPORT DocumentRepository
import com.aurionpro.app.repository.EmployeeRepository;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.repository.PaySlipRepository;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.DocumentService;
import com.aurionpro.app.service.EmailService;
import com.aurionpro.app.service.EmployeeService;
import com.aurionpro.app.service.PdfExportService;
import com.aurionpro.app.service.UserService;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final DocumentRepository documentRepository; // INJECT a DocumentRepository
	private final PasswordEncoder passwordEncoder;
	private final EmployeeMapper employeeMapper;
	private final UserService userService;
	private final DocumentService documentService;
	private final AuditLogService auditLogService;
	private final EmailService emailService;
	private final PaySlipRepository paySlipRepository;
	private final PaySlipMapper paySlipMapper;
	private final PdfExportService pdfExportService;

	// ... (addEmployee method is unchanged) ...
	@Override
	@Transactional
	public EmployeeResponseDto addEmployee(EmployeeCreateRequestDto createDto) {
		User orgUser = userService.getCurrentUser();
		Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
				.orElseThrow(() -> new RuntimeException("Organization not found for the current user."));

		validateOrganizationIsActive(organization);

		if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
			throw new DuplicateResourceException("User", "email", createDto.getEmail());
		}

		String temporaryPassword = RandomStringUtils.randomAlphanumeric(10);
		User employeeUser = new User();
		employeeUser.setEmail(createDto.getEmail());
		employeeUser.setPassword(passwordEncoder.encode(temporaryPassword));
		employeeUser.setEnabled(true);
		employeeUser.setPasswordChangeRequired(true);
		Role employeeRole = roleRepository.findByName(RoleType.ROLE_EMPLOYEE)
				.orElseThrow(() -> new RuntimeException("Critical Error: ROLE_EMPLOYEE is not found."));
		employeeUser.getRoles().add(employeeRole);

		Employee employee = new Employee();
		employee.setFirstName(createDto.getFirstName());
		employee.setLastName(createDto.getLastName());
		employee.setDateOfBirth(createDto.getDateOfBirth());
		employee.setOrganization(organization);
		employee.setUser(employeeUser);
		employee.setStatus(EmployeeStatus.PENDING_DOCUMENTS);

		Employee savedEmployee = employeeRepository.save(employee);

		String details = String.format("Created new employee '%s %s'", savedEmployee.getFirstName(),
				savedEmployee.getLastName());
		auditLogService.logAction(orgUser, ActionType.CREATE, "Employee", savedEmployee.getId(), details);

		emailService.sendEmployeeCredentialsEmail(savedEmployee.getUser().getEmail(), temporaryPassword);

		return employeeMapper.toDto(savedEmployee);
	}


	// ... (submitDocuments and activateEmployee methods are unchanged) ...
	@Override
	@Transactional
	public EmployeeResponseDto submitDocuments(String employeeEmail, List<MultipartFile> files) throws IOException {
		Employee employee = employeeRepository.findByUserEmail(employeeEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "email", employeeEmail));

		if (employee.getStatus() != EmployeeStatus.PENDING_DOCUMENTS) {
			throw new InvalidStateException("Employee is not in a state to submit documents.");
		}

		for (MultipartFile file : files) {
			documentService.uploadFile(file, "Employee", employee.getId());
		}

		employee.setStatus(EmployeeStatus.PENDING_APPROVAL);
		employee.setRejectionReason(null); // Clear previous rejection reason
		Employee updatedEmployee = employeeRepository.save(employee);

		auditLogService.logAction(employee.getUser(), ActionType.UPDATE, "Employee", updatedEmployee.getId(),
				"Submitted documents for approval.");

		return employeeMapper.toDto(updatedEmployee);
	}

	@Override
	@Transactional
	public EmployeeResponseDto activateEmployee(Long employeeId, EmployeeFinalizeRequestDto finalizeDto) {
		User orgUser = userService.getCurrentUser();
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

		validateOrganizationIsActive(employee.getOrganization());

		if (employee.getStatus() != EmployeeStatus.PENDING_APPROVAL) {
			throw new InvalidStateException("Employee is not pending approval.");
		}

		// --- MODIFIED LOGIC STARTS HERE ---

		// Instead of creating new objects, we get the existing ones (or create if they don't exist)
		BankAccountDetails bankAccount = employee.getBankAccount();
		if (bankAccount == null) {
			bankAccount = new BankAccountDetails();
		}
		// Update the fields on the existing or new object
		bankAccount.setAccountNumber(finalizeDto.getAccountNumber());
		bankAccount.setIfscCode(finalizeDto.getIfscCode());
		bankAccount.setBankName(finalizeDto.getBankName());

		SalaryStructure salary = employee.getSalaryStructure();
		if (salary == null) {
			salary = new SalaryStructure();
		}
		// Update the fields on the existing or new object
		salary.setBasicSalary(finalizeDto.getBasicSalary());
		salary.setHra(finalizeDto.getHra());
		salary.setDa(finalizeDto.getDa());
		salary.setPf(finalizeDto.getPf());
		salary.setOtherAllowances(finalizeDto.getOtherAllowances());
		
		// Set the updated or new objects back on the employee
		employee.setEmployeeNumber(finalizeDto.getEmployeeNumber());
		employee.setBankAccount(bankAccount);
		employee.setSalaryStructure(salary);
		employee.setStatus(EmployeeStatus.ACTIVE);
		employee.setRejectionReason(null); // Clear previous rejection reason

		// --- MODIFIED LOGIC ENDS HERE ---

		Employee updatedEmployee = employeeRepository.save(employee);

		auditLogService.logAction(orgUser, ActionType.ACTIVATE_EMPLOYEE, "Employee", updatedEmployee.getId(),
				"Employee account activated.");
		emailService.sendEmployeeActivationEmail(updatedEmployee.getUser().getEmail());

		return employeeMapper.toDto(updatedEmployee);
	}

	// --- THIS METHOD IS MODIFIED ---
	@Override
	@Transactional
	public EmployeeResponseDto rejectDocuments(Long employeeId, String reason) {
		User orgUser = userService.getCurrentUser();
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

		validateOrganizationIsActive(employee.getOrganization());

		if (employee.getStatus() != EmployeeStatus.PENDING_APPROVAL) {
			throw new InvalidStateException("Employee is not pending document approval.");
		}
		
		// 1. Find all existing documents for this employee
        List<Document> documentsToDelete = documentRepository.findByEntityNameAndEntityId("Employee", employeeId);

        // 2. Delete them from the database
        if (!documentsToDelete.isEmpty()) {
            documentRepository.deleteAll(documentsToDelete);
            // NOTE: A more advanced implementation would also delete the files from Cloudinary,
            // but for this project, just deleting the DB records is sufficient.
        }

		// 3. Update the employee's status and rejection reason
		employee.setStatus(EmployeeStatus.PENDING_DOCUMENTS);
		employee.setRejectionReason(reason);
		Employee updatedEmployee = employeeRepository.save(employee);

		// 4. Log the action and send the notification email
		auditLogService.logAction(orgUser, ActionType.REJECT_REQUEST, "Employee", updatedEmployee.getId(),
				"Documents rejected. Reason: " + reason);
		emailService.sendEmployeeDocumentRejectionEmail(updatedEmployee.getUser().getEmail(), reason);

		return employeeMapper.toDto(updatedEmployee);
	}

	// ... (rest of the file is unchanged) ...
	@Override
	public List<EmployeeResponseDto> getAllEmployeesForCurrentOrganization() {
		User orgUser = userService.getCurrentUser();
		Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
				.orElseThrow(() -> new RuntimeException("Organization not found for current user."));

		validateOrganizationIsActive(organization);

		return employeeRepository.findAllByOrganizationId(organization.getId()).stream().map(employeeMapper::toDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void softDeleteEmployee(Long employeeId) {
		User orgUser = userService.getCurrentUser();
		Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
				.orElseThrow(() -> new InvalidStateException("Organization not found for the current user."));

		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

		if (!employee.getOrganization().getId().equals(organization.getId())) {
			throw new SecurityException("You do not have permission to delete this employee.");
		}

		employee.setStatus(EmployeeStatus.DISABLED);
		employee.setDeleted(true);
		User employeeUser = employee.getUser();
		if (employeeUser != null) {
			employeeUser.setEnabled(false);
		}
		employeeRepository.save(employee);

		String details = String.format("Soft-deleted employee '%s %s' (ID: %d)", employee.getFirstName(),
				employee.getLastName(), employee.getId());
		auditLogService.logAction(orgUser, ActionType.DELETE, "Employee", employee.getId(), details);
	}
	
	@Override
	@Transactional
	public EmployeeResponseDto enableEmployee(Long employeeId) {
	    User orgUser = userService.getCurrentUser();
	    Employee employee = findEmployeeByIdAndOrg(employeeId);

	    if (employee.getStatus() != EmployeeStatus.DISABLED) {
	        throw new InvalidStateException("Employee is not in a DISABLED state.");
	    }

		// --- THIS IS THE FIX ---
		// 1. Find and delete all existing documents for this employee
        List<Document> documentsToDelete = documentRepository.findByEntityNameAndEntityId("Employee", employeeId);
        if (!documentsToDelete.isEmpty()) {
            documentRepository.deleteAll(documentsToDelete);
        }
		// --- END OF FIX ---

	    // 2. Reset the employee's status to begin the document upload process again
	    employee.setStatus(EmployeeStatus.PENDING_DOCUMENTS);
	    employee.setDeleted(false);
	    
	    User employeeUser = employee.getUser();
	    if (employeeUser != null) {
	        employeeUser.setEnabled(true); // Re-enable their login
	    }

	    Employee updatedEmployee = employeeRepository.save(employee);
	    
	    auditLogService.logAction(orgUser, ActionType.UPDATE, "Employee", updatedEmployee.getId(), "Employee account re-enabled.");
	    
	    return employeeMapper.toDto(updatedEmployee);
	}

	@Override
	public Page<EligibleEmployeeDto> getEligibleEmployeesForPayroll(Pageable pageable) {
		User orgUser = userService.getCurrentUser();
		Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", orgUser.getEmail()));

		 // 1. Get the current pay period
        YearMonth currentPayPeriod = YearMonth.now();

        // 2. Find all employee IDs that already have a payslip for this period
        List<Long> paidEmployeeIds = paySlipRepository.findEmployeeIdsPaidForPeriod(currentPayPeriod, organization.getId());

        Page<Employee> employees;

        // 3. Call the correct repository method based on whether anyone has been paid
        if (paidEmployeeIds.isEmpty()) {
            // If no one has been paid, use the original, faster query
            employees = employeeRepository.findEligibleForPayroll(organization.getId(), pageable);
        } else {
            // If some employees have been paid, use the new query to exclude them
            employees = employeeRepository.findEligibleForPayrollExcludingIds(organization.getId(), paidEmployeeIds, pageable);
        }
		return employees.map(this::mapToEligibleEmployeeDto);
	}

	private EligibleEmployeeDto mapToEligibleEmployeeDto(Employee employee) {
		BigDecimal netSalary = calculateNetSalary(employee.getSalaryStructure());
		String fullName = employee.getFirstName() + " " + employee.getLastName();
		return new EligibleEmployeeDto(employee.getId(), employee.getEmployeeNumber(), fullName, netSalary);
	}

	private BigDecimal calculateNetSalary(SalaryStructure s) {
		if (s == null)
			return BigDecimal.ZERO;
		BigDecimal earnings = s.getBasicSalary().add(s.getHra()).add(s.getDa()).add(s.getOtherAllowances());
		BigDecimal deductions = s.getPf();
		return earnings.subtract(deductions);
	}

	@Override
	public Page<PaySlipSummaryDto> getMyPaySlipHistory(Pageable pageable) {
		User currentUser = userService.getCurrentUser();
		Employee employee = employeeRepository.findByUserEmail(currentUser.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "userEmail", currentUser.getEmail()));
		Pageable sortedByPayPeriodDesc = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by("payPeriod").descending());
		Page<PaySlip> paySlipsPage = paySlipRepository.findByEmployeeId(employee.getId(), sortedByPayPeriodDesc);
		return paySlipsPage.map(paySlipMapper::toSummaryDto);
	}

	@Override
	public void downloadMyPaySlip(Long paySlipId, HttpServletResponse response) throws DocumentException, IOException {
		User currentUser = userService.getCurrentUser();
		Employee employee = employeeRepository.findByUserEmail(currentUser.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "userEmail", currentUser.getEmail()));

		PaySlip paySlip = paySlipRepository.findById(paySlipId)
				.orElseThrow(() -> new ResourceNotFoundException("PaySlip", "id", paySlipId));

		if (!paySlip.getEmployee().getId().equals(employee.getId())) {
			throw new SecurityException("You are not authorized to download this payslip.");
		}

		PaySlipDetailDto payslipDto = paySlipMapper.toPaySlipDetailDto(paySlip);
		response.setContentType("application/pdf");
		String fileName = String.format("PaySlip-%s-%s.pdf", employee.getFirstName(),
				paySlip.getPayPeriod().toString());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		pdfExportService.generatePaySlipPdf(payslipDto, response.getOutputStream());
	}

	private void validateOrganizationIsActive(Organization organization) {
		if (organization.getStatus() != OrganizationStatus.ACTIVE) {
			throw new InvalidStateException("Organization is not active and cannot perform this action.");
		}
	}

	private Employee findEmployeeByIdAndOrg(Long employeeId) {
		User orgUser = userService.getCurrentUser();
		Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
				.orElseThrow(() -> new InvalidStateException("Organization not found for current user."));

		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

		if (!employee.getOrganization().getId().equals(organization.getId())) {
			throw new SecurityException("You do not have permission to modify this employee.");
		}
		return employee;
	}

	@Override
	public EmployeeResponseDto getEmployeeById(Long employeeId) {
		Employee employee = findEmployeeByIdAndOrg(employeeId);
		return employeeMapper.toDto(employee);
	}

	@Override
	@Transactional
	public EmployeeResponseDto updateBankAccount(Long employeeId, UpdateBankAccountDto dto) {
		Employee employee = findEmployeeByIdAndOrg(employeeId);
		BankAccountDetails bankAccount = employee.getBankAccount();
		if (bankAccount == null)
			bankAccount = new BankAccountDetails();
		bankAccount.setAccountNumber(dto.getAccountNumber());
		bankAccount.setIfscCode(dto.getIfscCode());
		bankAccount.setBankName(dto.getBankName());
		employee.setBankAccount(bankAccount);
		Employee updatedEmployee = employeeRepository.save(employee);
		auditLogService.logAction(userService.getCurrentUser(), ActionType.UPDATE, "Employee", updatedEmployee.getId(),
				"Bank account details updated.");
		return employeeMapper.toDto(updatedEmployee);
	}

	@Override
	@Transactional
	public EmployeeResponseDto updateSalaryStructure(Long employeeId, UpdateSalaryStructureDto dto) {
		Employee employee = findEmployeeByIdAndOrg(employeeId);
		SalaryStructure salary = employee.getSalaryStructure();
		if (salary == null)
			salary = new SalaryStructure();
		salary.setBasicSalary(dto.getBasicSalary());
		salary.setHra(dto.getHra());
		salary.setDa(dto.getDa());
		salary.setPf(dto.getPf());
		salary.setOtherAllowances(dto.getOtherAllowances());
		employee.setSalaryStructure(salary);
		Employee updatedEmployee = employeeRepository.save(employee);
		auditLogService.logAction(userService.getCurrentUser(), ActionType.UPDATE, "Employee", updatedEmployee.getId(),
				"Salary structure updated.");
		return employeeMapper.toDto(updatedEmployee);
	}
	
	@Override
	public EmployeeResponseDto getMyEmployeeDetails(Authentication authentication) {
		String email = authentication.getName();
		Employee employee = employeeRepository.findByUserEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "userEmail", email));
		return employeeMapper.toDto(employee);
	}

}