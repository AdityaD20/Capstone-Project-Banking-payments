package com.aurionpro.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.DepositResponseDto;
import com.aurionpro.app.dto.DocumentDto;
import com.aurionpro.app.dto.FinalApprovalRequestDto;
import com.aurionpro.app.dto.OrganizationDto;
import com.aurionpro.app.dto.PaymentRequestResponseDto;
import com.aurionpro.app.dto.RejectionRequestDto;
import com.aurionpro.app.service.BankAdminService;
import com.aurionpro.app.service.DepositService;
import com.aurionpro.app.service.DocumentService;
import com.aurionpro.app.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-admin/organizations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BANK_ADMIN')")
@CrossOrigin(origins = "http://localhost:4200")
public class BankAdminController {

	private final OrganizationService organizationService;
	private final BankAdminService bankAdminService;
	private final DepositService depositService;
	private final DocumentService documentService;

	@PostMapping("/{id}/approve-initial")
	public ResponseEntity<OrganizationDto> approveInitialRegistration(@PathVariable Long id) {
		OrganizationDto updatedOrg = organizationService.approveInitialRegistration(id);
		return ResponseEntity.ok(updatedOrg);
	}

	@PostMapping("/{id}/approve-final")
	public ResponseEntity<OrganizationDto> approveFinalRegistration(@PathVariable Long id,
			@RequestBody FinalApprovalRequestDto requestDto) {
		OrganizationDto updatedOrg = organizationService.approveFinalRegistration(id, requestDto);
		return ResponseEntity.ok(updatedOrg);
	}

	@PostMapping("/{id}/reject-initial")
	public ResponseEntity<OrganizationDto> rejectInitialRegistration(@PathVariable Long id,
			@RequestBody RejectionRequestDto requestDto) {
		OrganizationDto updatedOrg = organizationService.rejectInitialRegistration(id, requestDto.getReason());
		return ResponseEntity.ok(updatedOrg);
	}

	@PostMapping("/{id}/reject-documents")
	public ResponseEntity<OrganizationDto> rejectDocumentApproval(@PathVariable Long id,
			@RequestBody RejectionRequestDto requestDto) {
		OrganizationDto updatedOrg = organizationService.rejectDocumentApproval(id, requestDto.getReason());
		return ResponseEntity.ok(updatedOrg);
	}

	@GetMapping("/payment-requests/pending")
	public ResponseEntity<List<PaymentRequestResponseDto>> getPendingPaymentRequests() {
		return ResponseEntity.ok(bankAdminService.getPendingPaymentRequests());
	}

	@PostMapping("/payment-requests/{id}/approve")
	public ResponseEntity<PaymentRequestResponseDto> approvePaymentRequest(@PathVariable Long id) {
		return ResponseEntity.ok(bankAdminService.approvePaymentRequest(id));
	}

	@PostMapping("/payment-requests/{id}/reject")
	public ResponseEntity<PaymentRequestResponseDto> rejectPaymentRequest(@PathVariable Long id,
			@RequestBody RejectionRequestDto rejectionDto) {
		return ResponseEntity.ok(bankAdminService.rejectPaymentRequest(id, rejectionDto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrganizationDto> getOrganizationById(@PathVariable Long id) {
		OrganizationDto organizationDto = organizationService.getOrganizationById(id);
		return ResponseEntity.ok(organizationDto);
	}

	@GetMapping
	public ResponseEntity<List<OrganizationDto>> getAllOrganizations(
			@RequestParam(required = false) List<String> statuses) {
		List<OrganizationDto> organizations = organizationService.getAllOrganizations(statuses);
		return ResponseEntity.ok(organizations);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> softDeleteOrganization(@PathVariable Long id) {
		organizationService.softDeleteOrganization(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/deposits/pending")
	public ResponseEntity<List<DepositResponseDto>> getPendingDepositRequests() {
		return ResponseEntity.ok(depositService.getPendingDepositRequests());
	}

	@PostMapping("/deposits/{id}/approve")
	public ResponseEntity<DepositResponseDto> approveDepositRequest(@PathVariable Long id) {
		return ResponseEntity.ok(depositService.approveDeposit(id));
	}

	@PostMapping("/deposits/{id}/reject")
	public ResponseEntity<DepositResponseDto> rejectDepositRequest(@PathVariable Long id,
			@RequestBody RejectionRequestDto rejectionDto) {
		return ResponseEntity.ok(depositService.rejectDeposit(id, rejectionDto));
	}

	@GetMapping("/{id}/documents")
	public ResponseEntity<List<DocumentDto>> getOrganizationDocuments(@PathVariable Long id) {
		return ResponseEntity.ok(documentService.getDocumentsForEntity("Organization", id));
	}

	@GetMapping("/deposits/{id}/document")
	public ResponseEntity<List<DocumentDto>> getDepositDocument(@PathVariable Long id) {
		return ResponseEntity.ok(documentService.getDocumentsForEntity("DepositRequest", id));
	}
}