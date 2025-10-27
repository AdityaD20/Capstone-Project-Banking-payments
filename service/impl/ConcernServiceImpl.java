package com.aurionpro.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.ConcernCommentRequestDto;
import com.aurionpro.app.dto.ConcernResponseDto;
import com.aurionpro.app.dto.ConcernStatusUpdateRequestDto;
import com.aurionpro.app.entity.Concern;
import com.aurionpro.app.entity.ConcernResponse;
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.enums.ConcernStatus;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.ConcernMapper;
import com.aurionpro.app.repository.ConcernRepository;
import com.aurionpro.app.repository.ConcernResponseRepository;
import com.aurionpro.app.repository.EmployeeRepository;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.ConcernService;
import com.aurionpro.app.service.DocumentService;
import com.aurionpro.app.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcernServiceImpl implements ConcernService {

    private final ConcernRepository concernRepository;
    private final ConcernResponseRepository concernResponseRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final DocumentService documentService;
    private final AuditLogService auditLogService;
    private final ConcernMapper concernMapper;

    // == EMPLOYEE METHODS ==
    @Override
    @Transactional
    public ConcernResponseDto createConcern(String description, MultipartFile attachment) throws IOException {
        User currentUser = userService.getCurrentUser();
        Employee employee = employeeRepository.findByUserEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "userEmail", currentUser.getEmail()));

        Concern concern = new Concern();
        concern.setEmployee(employee);
        concern.setDescription(description);
        concern.setStatus(ConcernStatus.OPEN);

        Concern savedConcern = concernRepository.save(concern);

        if (attachment != null && !attachment.isEmpty()) {
            documentService.uploadFile(attachment, "Concern", savedConcern.getId());
        }

        auditLogService.logAction(currentUser, ActionType.CREATE, "Concern", savedConcern.getId(), "Employee raised a new concern.");
        return concernMapper.toDto(savedConcern);
    }

    @Override
    public Page<ConcernResponseDto> getMyConcerns(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Employee employee = employeeRepository.findByUserEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "userEmail", currentUser.getEmail()));

        return concernRepository.findByEmployeeId(employee.getId(), pageable).map(concernMapper::toDto);
    }

    // == ORGANIZATION METHODS ==
    @Override
    public Page<ConcernResponseDto> getAllConcernsForOrganization(ConcernStatus status, Pageable pageable) {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new InvalidStateException("Organization not found for the current user."));

        Page<Concern> concerns;
        if (status != null) {
            concerns = concernRepository.findByEmployeeOrganizationIdAndStatus(organization.getId(), status, pageable);
        } else {
            concerns = concernRepository.findByEmployeeOrganizationId(organization.getId(), pageable);
        }
        return concerns.map(concernMapper::toDto);
    }

    @Override
    @Transactional
    public void addResponseToConcern(Long concernId, ConcernCommentRequestDto commentDto) {
        User orgUser = userService.getCurrentUser();
        Concern concern = findConcernAndVerifyOwnership(concernId, orgUser);

        ConcernResponse response = new ConcernResponse();
        response.setConcern(concern);
        response.setAuthor(orgUser);
        response.setResponseText(commentDto.getResponseText());

        concernResponseRepository.save(response);

        String details = String.format("Responded to concern #%d with: '%s'", concernId, commentDto.getResponseText());
        auditLogService.logAction(orgUser, ActionType.UPDATE, "Concern", concernId, details);
    }

    @Override
    @Transactional
    public ConcernResponseDto updateConcernStatus(Long concernId, ConcernStatusUpdateRequestDto statusUpdateDto) {
        User orgUser = userService.getCurrentUser();
        Concern concern = findConcernAndVerifyOwnership(concernId, orgUser);

        concern.setStatus(statusUpdateDto.getNewStatus());
        Concern updatedConcern = concernRepository.save(concern);

        String details = String.format("Updated status of concern #%d to %s", concernId, statusUpdateDto.getNewStatus());
        auditLogService.logAction(orgUser, ActionType.UPDATE, "Concern", concernId, details);
        
        return concernMapper.toDto(updatedConcern);
    }
    
    @Override
	public List<ConcernResponseDto> getResponsesForMyConcern(Long concernId) {
		// 1. Get the currently logged-in employee
		User currentUser = userService.getCurrentUser();
		Employee employee = employeeRepository.findByUserEmail(currentUser.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "userEmail", currentUser.getEmail()));

		// 2. Find the concern and verify that this employee is the owner
		Concern concern = concernRepository.findById(concernId)
				.orElseThrow(() -> new ResourceNotFoundException("Concern", "id", concernId));
		
		if (!concern.getEmployee().getId().equals(employee.getId())) {
			throw new SecurityException("You do not have permission to view responses for this concern.");
		}

		// 3. If ownership is verified, fetch and return the responses
		List<ConcernResponse> responses = concernResponseRepository.findAllByConcernIdOrderByCreatedAtAsc(concernId);
		return responses.stream()
			.map(concernMapper::concernResponseToDto)
			.collect(Collectors.toList());
	}
    

    private Concern findConcernAndVerifyOwnership(Long concernId, User orgUser) {
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new InvalidStateException("Organization not found for the current user."));

        Concern concern = concernRepository.findById(concernId)
                .orElseThrow(() -> new ResourceNotFoundException("Concern", "id", concernId));

        if (!concern.getEmployee().getOrganization().getId().equals(organization.getId())) {
            throw new SecurityException("You do not have permission to manage this concern.");
        }
        return concern;
    }
    
    
    
}