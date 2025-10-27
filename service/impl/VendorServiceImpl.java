package com.aurionpro.app.service.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.dto.VendorCreateRequestDto;
import com.aurionpro.app.dto.VendorResponseDto;
import com.aurionpro.app.entity.BankAccountDetails;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.Vendor;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.enums.OrganizationStatus;
import com.aurionpro.app.entity.enums.RoleType;
import com.aurionpro.app.entity.enums.VendorStatus;
import com.aurionpro.app.entity.user.Role;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.DuplicateResourceException;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.PaymentRequestMapper;
import com.aurionpro.app.mapper.VendorMapper;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.repository.PaymentRequestRepository;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.VendorRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.EmailService;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.service.VendorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

	private final VendorRepository vendorRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserService userService;
	private final AuditLogService auditLogService;
	private final EmailService emailService;
	private final VendorMapper vendorMapper;
	private final PaymentRequestRepository paymentRequestRepository;
	private final PaymentRequestMapper paymentRequestMapper;

	@Override
	@Transactional
	public VendorResponseDto addVendor(VendorCreateRequestDto createDto) {
		// 1. Get current user (organization) and validate its status
		User orgUser = userService.getCurrentUser();
		Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
				.orElseThrow(() -> new InvalidStateException("Organization not found for the current user."));

		if (organization.getStatus() != OrganizationStatus.ACTIVE) {
			throw new InvalidStateException("Organization is not active and cannot add vendors.");
		}

		// 2. Check if a user with the vendor's email already exists
		if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
			throw new DuplicateResourceException("User", "email", createDto.getEmail());
		}

		// 3. System generates a secure random password
		String temporaryPassword = RandomStringUtils.randomAlphanumeric(12);

		// 4. Create a User account for the Vendor
		User vendorUser = new User();
		vendorUser.setEmail(createDto.getEmail());
		vendorUser.setPassword(passwordEncoder.encode(temporaryPassword));
		vendorUser.setEnabled(true);
		vendorUser.setPasswordChangeRequired(true); // Still force password change for security
		Role vendorRole = roleRepository.findByName(RoleType.ROLE_VENDOR)
				.orElseThrow(() -> new RuntimeException("CRITICAL: ROLE_VENDOR not found in database."));
		vendorUser.getRoles().add(vendorRole);

		// 5. Create the BankAccountDetails entity from the DTO
		BankAccountDetails bankAccount = new BankAccountDetails();
		bankAccount.setAccountNumber(createDto.getAccountNumber());
		bankAccount.setIfscCode(createDto.getIfscCode());
		bankAccount.setBankName(createDto.getBankName());

		// 6. Create the Vendor entity and link everything
		Vendor vendor = new Vendor();
		vendor.setName(createDto.getName());
		vendor.setContactEmail(createDto.getEmail());
		vendor.setContactPhone(createDto.getPhone());
		vendor.setOrganization(organization);
		vendor.setUser(vendorUser);
		vendor.setBankAccount(bankAccount); // Link the new bank account
		vendor.setStatus(VendorStatus.ACTIVE); // Set status to ACTIVE immediately

		// 7. Save the new vendor (Cascade will save the User and BankAccount)
		Vendor savedVendor = vendorRepository.save(vendor);

		// 8. Log the action
		String details = String.format("Created and activated new vendor '%s' (%s)", savedVendor.getName(),
				savedVendor.getContactEmail());
		auditLogService.logAction(orgUser, ActionType.CREATE, "Vendor", savedVendor.getId(), details);

		// 9. Send credentials email to the vendor
		emailService.sendVendorCredentialsEmail(savedVendor.getContactEmail(), temporaryPassword);

		// 10. Return the mapped DTO
		return vendorMapper.toDto(savedVendor);
	}
	
	@Override
    public Page<VendorResponseDto> getAllVendors(Pageable pageable) {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", orgUser.getEmail()));

        Page<Vendor> vendors = vendorRepository.findByOrganizationIdAndDeletedFalse(organization.getId(), pageable);
        return vendors.map(vendorMapper::toDto);
    }
}