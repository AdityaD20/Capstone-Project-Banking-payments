package com.aurionpro.app.service;

import com.aurionpro.app.dto.FinalApprovalRequestDto;
import com.aurionpro.app.dto.OrganizationDto;
import com.aurionpro.app.dto.OrganizationRegistrationRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface OrganizationService {

	OrganizationDto registerOrganization(OrganizationRegistrationRequestDto requestDto);

	OrganizationDto approveInitialRegistration(Long organizationId);

	OrganizationDto approveFinalRegistration(Long organizationId, FinalApprovalRequestDto approvalDto);

	OrganizationDto rejectInitialRegistration(Long organizationId, String reason);

	OrganizationDto rejectDocumentApproval(Long organizationId, String reason);

	OrganizationDto submitDocuments(String userEmail, List<MultipartFile> files) throws IOException;

	List<OrganizationDto> findOrganizationsByStatus(String status);

	OrganizationDto getOrganizationById(Long organizationId);

	List<OrganizationDto> getAllOrganizations(List<String> statuses);

	void softDeleteOrganization(Long organizationId);
	
	OrganizationDto getMyOrganizationDetails();
}