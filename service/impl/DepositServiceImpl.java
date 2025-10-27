package com.aurionpro.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.DepositRequestDto;
import com.aurionpro.app.dto.DepositResponseDto;
import com.aurionpro.app.dto.RejectionRequestDto;
import com.aurionpro.app.entity.DepositRequest;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.enums.OrganizationStatus;
import com.aurionpro.app.entity.enums.RequestStatus;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.DepositRequestMapper;
import com.aurionpro.app.repository.DepositRequestRepository;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.DepositService;
import com.aurionpro.app.service.DocumentService;
import com.aurionpro.app.service.EmailService;
import com.aurionpro.app.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final DepositRequestRepository depositRequestRepository;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final DocumentService documentService;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final DepositRequestMapper depositRequestMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void requestDeposit(String requestDtoJson, MultipartFile file) throws IOException {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new InvalidStateException("Organization not found for the current user."));

        if (organization.getStatus() != OrganizationStatus.ACTIVE) {
            throw new InvalidStateException("Organization is not active and cannot request a deposit.");
        }

        if (file == null || file.isEmpty()) {
            throw new InvalidStateException("A proof of deposit document is required.");
        }

        DepositRequestDto requestDto;
        try {
            requestDto = objectMapper.readValue(requestDtoJson, DepositRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new InvalidStateException("Invalid format for deposit data. Please check the JSON structure.");
        }

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setOrganization(organization);
        depositRequest.setAmount(requestDto.getAmount());
        depositRequest.setDescription(requestDto.getDescription());
        depositRequest.setStatus(RequestStatus.PENDING);

        DepositRequest savedRequest = depositRequestRepository.save(depositRequest);

        // Upload and associate the document
        documentService.uploadFile(file, "DepositRequest", savedRequest.getId());

        String logDetails = String.format("Requested a deposit of amount %s", savedRequest.getAmount());
        auditLogService.logAction(orgUser, ActionType.CREATE, "DepositRequest", savedRequest.getId(), logDetails);
    }

    @Override
    public List<DepositResponseDto> getPendingDepositRequests() {
        return depositRequestRepository.findByStatus(RequestStatus.PENDING)
                .stream()
                .map(depositRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepositResponseDto approveDeposit(Long depositRequestId) {
        User adminUser = userService.getCurrentUser();
        DepositRequest request = depositRequestRepository.findById(depositRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositRequest", "id", depositRequestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This deposit request is not pending approval.");
        }

        Organization organization = request.getOrganization();
        organization.getBankAccount().setBalance(organization.getBankAccount().getBalance().add(request.getAmount()));
        organizationRepository.save(organization);

        request.setStatus(RequestStatus.APPROVED);
        DepositRequest updatedRequest = depositRequestRepository.save(request);

        String logDetails = "Deposit request approved. Balance updated.";
        auditLogService.logAction(adminUser, ActionType.APPROVE_REQUEST, "DepositRequest", updatedRequest.getId(), logDetails);
        emailService.sendDepositApprovalEmail(organization.getUser().getEmail(), updatedRequest.getAmount());

        return depositRequestMapper.toDto(updatedRequest);
    }

    @Override
    @Transactional
    public DepositResponseDto rejectDeposit(Long depositRequestId, RejectionRequestDto rejectionDto) {
        User adminUser = userService.getCurrentUser();
        DepositRequest request = depositRequestRepository.findById(depositRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositRequest", "id", depositRequestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This deposit request is not pending approval.");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectionDto.getReason());
        DepositRequest updatedRequest = depositRequestRepository.save(request);

        String logDetails = "Deposit request rejected. Reason: " + rejectionDto.getReason();
        auditLogService.logAction(adminUser, ActionType.REJECT_REQUEST, "DepositRequest", updatedRequest.getId(), logDetails);
        emailService.sendDepositRejectionEmail(request.getOrganization().getUser().getEmail(), rejectionDto.getReason());

        return depositRequestMapper.toDto(updatedRequest);
    }
    
    @Override
    public List<DepositResponseDto> getDepositHistoryForOrganization() {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", orgUser.getEmail()));

        List<DepositRequest> requests = depositRequestRepository
                .findAllByOrganizationIdOrderByCreatedAtDesc(organization.getId());

        return requests.stream()
                .map(depositRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}