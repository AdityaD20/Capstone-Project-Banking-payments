package com.aurionpro.app.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.FinalApprovalRequestDto;
import com.aurionpro.app.dto.OrganizationDto;
import com.aurionpro.app.dto.OrganizationRegistrationRequestDto;
import com.aurionpro.app.entity.BankAccountDetails;
import com.aurionpro.app.entity.Document; // IMPORT Document
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.enums.OrganizationStatus;
import com.aurionpro.app.entity.enums.RoleType;
import com.aurionpro.app.entity.user.Role;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.DuplicateResourceException;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.OrganizationMapper;
import com.aurionpro.app.repository.DocumentRepository; // IMPORT DocumentRepository
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.DocumentService;
import com.aurionpro.app.service.EmailService;
import com.aurionpro.app.service.OrganizationService;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.util.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final DocumentRepository documentRepository; // INJECT a DocumentRepository
	private final PasswordEncoder passwordEncoder;
	private final OrganizationMapper organizationMapper;
	private final DocumentService documentService;
	private final AuditLogService auditLogService;
	private final EmailService emailService;
	private final UserService userService;

	// ... (registerOrganization and approveInitialRegistration methods are
	// unchanged) ...
	@Override
	@Transactional
	public OrganizationDto registerOrganization(OrganizationRegistrationRequestDto requestDto) {
		if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
			throw new DuplicateResourceException("User", "email", requestDto.getEmail());
		}

		if (organizationRepository.existsByNameIgnoreCase(requestDto.getOrganizationName())) {
			throw new DuplicateResourceException("Organization", "name", requestDto.getOrganizationName());
		}

		if (!ValidationUtils.isPasswordStrong(requestDto.getPassword())) {
			throw new InvalidStateException(
					"Password does not meet complexity requirements. It must be at least 8 characters long and include an uppercase letter, a lowercase letter, a number, and a special symbol.");
		}

		User user = new User();
		user.setEmail(requestDto.getEmail());
		user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
		Role orgRole = roleRepository.findByName(RoleType.ROLE_ORGANIZATION)
				.orElseThrow(() -> new RuntimeException("Critical Error: ROLE_ORGANIZATION is not found."));
		user.getRoles().add(orgRole);
		user.setPasswordChangeRequired(false);

		Organization organization = new Organization();
		organization.setName(requestDto.getOrganizationName());
		organization.setStatus(OrganizationStatus.PENDING_INITIAL_APPROVAL);
		organization.setUser(user);

		Organization savedOrg = organizationRepository.save(organization);
		User newlyCreatedUser = savedOrg.getUser();

		auditLogService.logAction(newlyCreatedUser, ActionType.CREATE, "Organization", savedOrg.getId(),
				"New organization self-registered.");

		return organizationMapper.toDto(savedOrg);
	}

	@Override
	@Transactional
	public OrganizationDto approveInitialRegistration(Long organizationId) {
		User actor = userService.getCurrentUser();
		Organization org = findOrgById(organizationId);

		if (org.getStatus() != OrganizationStatus.PENDING_INITIAL_APPROVAL) {
			throw new InvalidStateException("Organization is not in PENDING_INITIAL_APPROVAL state.");
		}

		User userToEnable = org.getUser();
		userToEnable.setEnabled(true);
		userRepository.save(userToEnable);

		org.setStatus(OrganizationStatus.WAITING_FOR_DOCUMENTS);
		org.setRejectionReason(null); // Clear previous rejection reason if any
		Organization updatedOrg = organizationRepository.save(org);

		auditLogService.logAction(actor, ActionType.APPROVE_REQUEST, "Organization", updatedOrg.getId(),
				"Initial registration approved.");
		emailService.sendCredentialsEmail(updatedOrg.getUser().getEmail());

		return organizationMapper.toDto(updatedOrg);
	}

	// ... (submitDocuments and approveFinalRegistration methods are unchanged) ...
	@Override
	@Transactional
	public OrganizationDto submitDocuments(String userEmail, List<MultipartFile> files) throws IOException {
		User actor = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

		Organization org = organizationRepository.findByUserEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", userEmail));

		if (org.getStatus() != OrganizationStatus.WAITING_FOR_DOCUMENTS
				&& org.getStatus() != OrganizationStatus.REJECTED) {
			throw new InvalidStateException("Organization is not in a state to submit documents.");
		}
		if (files == null || files.isEmpty() || files.get(0).isEmpty()) {
			throw new InvalidStateException("At least one document must be uploaded.");
		}

		List<String> allowedMimeTypes = Arrays.asList("application/pdf", "image/jpeg");
		for (MultipartFile file : files) {
			String fileType = file.getContentType();
			if (fileType == null || !allowedMimeTypes.contains(fileType)) {
				throw new InvalidStateException(
						"Invalid file type: " + file.getOriginalFilename() + ". Only PDF and JPG files are allowed.");
			}
		}

		for (MultipartFile file : files) {
			documentService.uploadFile(file, "Organization", org.getId());
		}

		org.setStatus(OrganizationStatus.PENDING_FINAL_APPROVAL);
		org.setRejectionReason(null); // Clear previous rejection reason
		Organization updatedOrg = organizationRepository.save(org);

		auditLogService.logAction(actor, ActionType.UPDATE, "Organization", updatedOrg.getId(),
				"Documents submitted for final approval.");

		return organizationMapper.toDto(updatedOrg);
	}

	@Override
	@Transactional
	public OrganizationDto approveFinalRegistration(Long organizationId, FinalApprovalRequestDto approvalDto) {
		User actor = userService.getCurrentUser();
		Organization org = findOrgById(organizationId);

		if (org.getStatus() != OrganizationStatus.PENDING_FINAL_APPROVAL) {
			throw new InvalidStateException("Organization is not in PENDING_FINAL_APPROVAL state.");
		}
		if (approvalDto.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidStateException("Initial balance cannot be negative.");
		}

		BankAccountDetails account = new BankAccountDetails();
		account.setAccountNumber(approvalDto.getAccountNumber());
		account.setIfscCode(approvalDto.getIfscCode());
		account.setBankName(approvalDto.getBankName());
		account.setBalance(approvalDto.getInitialBalance());

		org.setBankAccount(account);
		org.setStatus(OrganizationStatus.ACTIVE);
		org.setRejectionReason(null); // Clear previous rejection reason

		Organization updatedOrg = organizationRepository.save(org);

		auditLogService.logAction(actor, ActionType.ACTIVATE_ORGANIZATION, "Organization", updatedOrg.getId(),
				"Final approval granted. Account is now active.");
		emailService.sendActivationEmail(updatedOrg.getUser().getEmail());

		return organizationMapper.toDto(updatedOrg);
	}

	// ... (rejectInitialRegistration method is unchanged) ...
	@Override
	@Transactional
	public OrganizationDto rejectInitialRegistration(Long organizationId, String reason) {
		User actor = userService.getCurrentUser();
		Organization org = findOrgById(organizationId);

		if (org.getStatus() != OrganizationStatus.PENDING_INITIAL_APPROVAL) {
			throw new InvalidStateException("Organization is not in PENDING_INITIAL_APPROVAL state.");
		}

		org.setStatus(OrganizationStatus.REJECTED);
		org.setRejectionReason(reason); // SET THE REASON
		Organization updatedOrg = organizationRepository.save(org);

		auditLogService.logAction(actor, ActionType.REJECT_REQUEST, "Organization", updatedOrg.getId(),
				"Initial registration rejected. Reason: " + reason);
		emailService.sendInitialRejectionEmail(updatedOrg.getUser().getEmail(), reason);

		return organizationMapper.toDto(updatedOrg);
	}

	// --- THIS METHOD IS MODIFIED ---
	@Override
	@Transactional
	public OrganizationDto rejectDocumentApproval(Long organizationId, String reason) {
		User actor = userService.getCurrentUser();
		Organization org = findOrgById(organizationId);

		if (org.getStatus() != OrganizationStatus.PENDING_FINAL_APPROVAL) {
			throw new InvalidStateException("Organization is not in PENDING_FINAL_APPROVAL state.");
		}

		// 1. Find all existing documents for this organization
		List<Document> documentsToDelete = documentRepository.findByEntityNameAndEntityId("Organization",
				organizationId);

		// 2. Delete them from the database
		if (!documentsToDelete.isEmpty()) {
			documentRepository.deleteAll(documentsToDelete);
		}

		// 3. Update the organization's status and rejection reason
		org.setStatus(OrganizationStatus.REJECTED);
		org.setRejectionReason(reason);
		Organization updatedOrg = organizationRepository.save(org);

		// 4. Log the action and send notification email
		auditLogService.logAction(actor, ActionType.REJECT_REQUEST, "Organization", updatedOrg.getId(),
				"Documents rejected. Reason: " + reason);
		emailService.sendDocumentRejectionEmail(updatedOrg.getUser().getEmail(), reason);

		return organizationMapper.toDto(updatedOrg);
	}

	// ... (rest of the file is unchanged) ...
	@Override
	public List<OrganizationDto> findOrganizationsByStatus(String status) {
		OrganizationStatus orgStatus = OrganizationStatus.valueOf(status.toUpperCase());
		return organizationRepository.findByStatus(orgStatus).stream().map(organizationMapper::toDto)
				.collect(Collectors.toList());
	}

	@Override
	public OrganizationDto getOrganizationById(Long organizationId) {
		return organizationMapper.toDto(findOrgById(organizationId));
	}

	private Organization findOrgById(Long organizationId) {
		return organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
	}

	@Override
	public List<OrganizationDto> getAllOrganizations(List<String> statuses) {
		List<Organization> organizations;
		if (statuses == null || statuses.isEmpty()) {
			// ** THE FIX IS HERE: Call the correct, explicit method **
			organizations = organizationRepository.findAllByDeletedFalse(); 
		} else {
			List<OrganizationStatus> orgStatuses = new ArrayList<>();
			for (String status : statuses) {
				try {
					orgStatuses.add(OrganizationStatus.valueOf(status.toUpperCase()));
				} catch (IllegalArgumentException e) {
					// Ignore invalid status strings
				}
			}
			// ** THE FIX IS HERE: Call the correct, explicit method **
			organizations = organizationRepository.findByStatusInAndDeletedFalse(orgStatuses);
		}
		return organizations.stream().map(organizationMapper::toDto).collect(Collectors.toList());
	}
    
    // This method is also critical. Ensure it's correct.
	@Override
	@Transactional
	public void softDeleteOrganization(Long organizationId) {
		User actor = userService.getCurrentUser();
		Organization organization = findOrgById(organizationId);

		if (organization.isDeleted()) {
			throw new InvalidStateException("Organization with ID " + organizationId + " is already deleted.");
		}

		organization.setDeleted(true);
        // Also update the status to DISABLED for clarity in the UI
        organization.setStatus(OrganizationStatus.DISABLED); 
		User orgUser = organization.getUser();
		if (orgUser != null) {
			orgUser.setEnabled(false);
		}
		organizationRepository.save(organization);

		String details = String.format("Soft-deleted organization '%s' (ID: %d)", organization.getName(), organization.getId());
		auditLogService.logAction(actor, ActionType.DELETE, "Organization", organization.getId(), details);
	}
	
	@Override
    public OrganizationDto getMyOrganizationDetails() {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", orgUser.getEmail()));
        return organizationMapper.toDto(organization);
    }

}