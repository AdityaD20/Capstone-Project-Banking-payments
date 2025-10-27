package com.aurionpro.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.dto.PaymentRequestResponseDto;
import com.aurionpro.app.dto.RejectionRequestDto;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.PaymentRequest;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.entity.enums.RequestStatus;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.PaymentRequestMapper;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.repository.PaySlipRepository;
import com.aurionpro.app.repository.PaymentRequestRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.BankAdminJobLauncherService;
import com.aurionpro.app.service.BankAdminService;
import com.aurionpro.app.service.EmailService;
import com.aurionpro.app.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAdminServiceImpl implements BankAdminService {

    private final JobLauncher jobLauncher;
    private final Job salaryDisbursementJob;
    private final PaymentRequestRepository paymentRequestRepository;
    private final OrganizationRepository organizationRepository;
    private final PaySlipRepository paySlipRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final PaymentRequestMapper paymentRequestMapper;
    private final EmailService emailService;
    private final BankAdminJobLauncherService jobLauncherService;

    @Override
    public List<PaymentRequestResponseDto> getPendingPaymentRequests() {
        return paymentRequestRepository.findByStatus(RequestStatus.PENDING)
                .stream()
                .map(paymentRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentRequestResponseDto approvePaymentRequest(Long paymentRequestId) {
        User adminUser = userService.getCurrentUser();
        PaymentRequest request = findRequestById(paymentRequestId);
        Organization organization = request.getOrganization();

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This payment request is not pending approval.");
        }

        if (organization.getBankAccount().getBalance().compareTo(request.getAmount()) < 0) {
            request.setStatus(RequestStatus.FAILED);
            request.setRejectionReason("Insufficient funds.");
        } else {
            organization.getBankAccount().setBalance(organization.getBankAccount().getBalance().subtract(request.getAmount()));
            request.setStatus(RequestStatus.PAID);
        }

        organizationRepository.save(organization);
        PaymentRequest updatedRequest = paymentRequestRepository.save(request);

       
        if (updatedRequest.getStatus() == RequestStatus.PAID && updatedRequest.getType() == PaymentType.SALARY) {
            jobLauncherService.launchPayslipGenerationJob(updatedRequest.getId());
        }

        auditLogService.logAction(adminUser, ActionType.APPROVE_REQUEST, "PaymentRequest", updatedRequest.getId(), "Payment request processed. Final status: " + updatedRequest.getStatus());
        if (updatedRequest.getStatus() == RequestStatus.PAID) {
            emailService.sendPaymentApprovedEmail(organization.getUser().getEmail(), updatedRequest.getId(), updatedRequest.getAmount());
        } else if (updatedRequest.getStatus() == RequestStatus.FAILED) {
            emailService.sendPaymentRejectedEmail(organization.getUser().getEmail(), updatedRequest.getId(), "Insufficient funds.");
        }
        
        return paymentRequestMapper.toDto(updatedRequest);
    }
    
    @Override
    @Transactional
    public PaymentRequestResponseDto rejectPaymentRequest(Long paymentRequestId, RejectionRequestDto rejectionDto) {
        User adminUser = userService.getCurrentUser();
        PaymentRequest request = findRequestById(paymentRequestId);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This payment request is not pending approval.");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectionDto.getReason());
        PaymentRequest updatedRequest = paymentRequestRepository.save(request);
        
        String logDetails = "Payment request rejected. Reason: " + rejectionDto.getReason();
        auditLogService.logAction(adminUser, ActionType.REJECT_REQUEST, "PaymentRequest", updatedRequest.getId(), logDetails);
        emailService.sendPaymentRejectedEmail(request.getOrganization().getUser().getEmail(), updatedRequest.getId(), rejectionDto.getReason());
        
        return paymentRequestMapper.toDto(updatedRequest);
    }
    
    private PaymentRequest findRequestById(Long id) {
        return paymentRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", id));
    }
}