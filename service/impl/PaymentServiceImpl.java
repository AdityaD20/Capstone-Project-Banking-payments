package com.aurionpro.app.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.dto.PaymentHistoryDto;
import com.aurionpro.app.dto.PaymentRequestDto;
import com.aurionpro.app.dto.PaymentRequestItemDto;
import com.aurionpro.app.dto.SalaryDisbursementRequestDto;
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.PaymentRequest;
import com.aurionpro.app.entity.PaymentRequestItem;
import com.aurionpro.app.entity.Vendor;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.enums.OrganizationStatus;
import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.entity.enums.RequestStatus;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.repository.EmployeeRepository;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.repository.PaySlipRepository;
import com.aurionpro.app.repository.PaymentRequestRepository;
import com.aurionpro.app.repository.VendorRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.PaymentService;
import com.aurionpro.app.service.PayrollService;
import com.aurionpro.app.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRequestRepository paymentRequestRepository;
	private final OrganizationRepository organizationRepository;
	private final EmployeeRepository employeeRepository;
	private final VendorRepository vendorRepository;
	private final UserService userService;
	private final PaySlipRepository paySlipRepository;
	private final PayrollService payrollService;
	private final AuditLogService auditLogService;

	@Override
	@Transactional
	public void createVendorPaymentRequest(PaymentRequestDto requestDto) {
		User orgUser = userService.getCurrentUser();
		Organization organization = getActiveOrganization(orgUser);

		// Find the vendor to be paid
		Vendor vendor = vendorRepository.findById(requestDto.getVendorId())
				.orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", requestDto.getVendorId()));

		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.setOrganization(organization);
		paymentRequest.setVendor(vendor);
		paymentRequest.setDescription(requestDto.getDescription());
		paymentRequest.setAmount(requestDto.getAmount());
		paymentRequest.setStatus(RequestStatus.PENDING);
		paymentRequest.setType(PaymentType.VENDOR);

		PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);

		String logDetails = String.format("Created vendor payment request for '%s' with amount %s", vendor.getName(),
				savedRequest.getAmount());
		auditLogService.logAction(orgUser, ActionType.CREATE, "Organization created PaymentRequest for vendor", savedRequest.getId(), logDetails);
	}

	@Override
	@Transactional
	public void initiateMonthlySalaryDisbursal(SalaryDisbursementRequestDto disbursementDto) {
		User orgUser = userService.getCurrentUser();
		Organization organization = getActiveOrganization(orgUser);

		YearMonth currentMonth = YearMonth.now();
		LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
		LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

		boolean pendingRequestExists = paymentRequestRepository
				.existsByOrganizationIdAndTypeAndStatusAndCreatedAtBetween(organization.getId(), PaymentType.SALARY,
						RequestStatus.PENDING, startOfMonth, endOfMonth);

		if (pendingRequestExists) {
			throw new InvalidStateException(
					"A salary disbursement request for this month is already pending approval and cannot be created again.");
		}

		if (disbursementDto.getEmployeesToPay() == null || disbursementDto.getEmployeesToPay().isEmpty()) {
			throw new InvalidStateException("At least one employee must be selected for payment.");
		}

		BigDecimal totalAmount = disbursementDto.getEmployeesToPay().stream().map(PaymentRequestItemDto::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		String description = String.format("%s Payroll for %d employees",
				YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
				disbursementDto.getEmployeesToPay().size());

		PaymentRequest request = new PaymentRequest();
		request.setOrganization(organization);
		request.setAmount(totalAmount);
		request.setStatus(RequestStatus.PENDING);
		request.setType(PaymentType.SALARY);
		request.setDescription(description);

		List<PaymentRequestItem> items = disbursementDto.getEmployeesToPay().stream().map(itemDto -> {
			Employee employee = employeeRepository.findById(itemDto.getEmployeeId())
					.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", itemDto.getEmployeeId()));

			PaymentRequestItem item = new PaymentRequestItem();
			item.setPaymentRequest(request);
			item.setEmployee(employee);
			item.setAmountPaid(itemDto.getAmount());
			return item;
		}).collect(Collectors.toList());

		request.setItems(items);

		PaymentRequest savedRequest = paymentRequestRepository.save(request);

		String logDetails = String.format("Initiated salary disbursement for %d employees with total amount %s",
				disbursementDto.getEmployeesToPay().size(), savedRequest.getAmount());
		auditLogService.logAction(orgUser, ActionType.CREATE, "Organization created PaymentRequest for salary disbursal", savedRequest.getId(), logDetails);
	}
	
	@Override
    public List<PaymentHistoryDto> getPaymentHistoryForOrganization() {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", orgUser.getEmail()));
        
        List<PaymentRequest> requests = paymentRequestRepository.findAllByOrganizationId(organization.getId());
        
        return requests.stream().map(req -> {
            PaymentHistoryDto dto = new PaymentHistoryDto();
            dto.setId(req.getId());
            dto.setAmount(req.getAmount());
            dto.setStatus(req.getStatus());
            dto.setDescription(req.getDescription());
            dto.setCreatedAt(req.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

	private Organization getActiveOrganization(User user) {
		Organization organization = organizationRepository.findByUserEmail(user.getEmail())
				.orElseThrow(() -> new RuntimeException("Organization not found for current user."));
		if (organization.getStatus() != OrganizationStatus.ACTIVE) {
			throw new InvalidStateException("Organization is not active and cannot perform payment actions.");
		}
		return organization;
	}
}