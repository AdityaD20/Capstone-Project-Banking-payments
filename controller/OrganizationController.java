package com.aurionpro.app.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.ConcernCommentRequestDto;
import com.aurionpro.app.dto.ConcernResponseDto;
import com.aurionpro.app.dto.ConcernStatusUpdateRequestDto;
import com.aurionpro.app.dto.DepositResponseDto;
import com.aurionpro.app.dto.DocumentDto;
import com.aurionpro.app.dto.EligibleEmployeeDto;
import com.aurionpro.app.dto.EmployeeCreateRequestDto;
import com.aurionpro.app.dto.EmployeeFinalizeRequestDto;
import com.aurionpro.app.dto.EmployeeResponseDto;
import com.aurionpro.app.dto.OrganizationDto;
import com.aurionpro.app.dto.PaymentHistoryDto;
import com.aurionpro.app.dto.RejectionRequestDto;
import com.aurionpro.app.dto.UpdateBankAccountDto;
import com.aurionpro.app.dto.UpdateSalaryStructureDto;
import com.aurionpro.app.dto.VendorCreateRequestDto;
import com.aurionpro.app.dto.VendorResponseDto;
import com.aurionpro.app.entity.enums.ConcernStatus;
import com.aurionpro.app.service.BulkImportService;
import com.aurionpro.app.service.ConcernService;
import com.aurionpro.app.service.DepositService;
import com.aurionpro.app.service.DocumentService;
import com.aurionpro.app.service.EmployeeService;
import com.aurionpro.app.service.OrganizationService;
import com.aurionpro.app.service.PaymentService;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.service.VendorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZATION')")
@CrossOrigin(origins = "http://localhost:4200")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final EmployeeService employeeService;
    private final VendorService vendorService;
    private final UserService userService;
    private final DepositService depositService;
    private final ConcernService concernService;
    private final DocumentService documentService;
    private final PaymentService paymentService;	
    private final BulkImportService bulkImportService;


    @GetMapping("/me")
    public ResponseEntity<OrganizationDto> getMyOrganization(Authentication authentication) {
    	return ResponseEntity.ok(organizationService.getMyOrganizationDetails());
    }

    @PostMapping(value = "/documents/upload" , consumes = "multipart/form-data")
    public ResponseEntity<OrganizationDto> uploadDocuments(@RequestParam("files") List<MultipartFile> files, Authentication authentication) throws IOException {
        String email = authentication.getName();
        OrganizationDto updatedOrg = organizationService.submitDocuments(email, files);
        return ResponseEntity.ok(updatedOrg);
    }
    
    @PostMapping("/employees")
    public ResponseEntity<EmployeeResponseDto> addEmployee(@RequestBody EmployeeCreateRequestDto createDto) {
        EmployeeResponseDto newEmployee = employeeService.addEmployee(createDto);
        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }
    
    @PostMapping(value = "/employees/batch-upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadEmployeeBatchFile(@RequestParam("file") MultipartFile file) throws Exception {
    	bulkImportService.runBulkImportJob(file);
        return new ResponseEntity<>("Batch job has been completed.", HttpStatus.ACCEPTED);
    }

    @PostMapping("/employees/{id}/activate")
    public ResponseEntity<EmployeeResponseDto> activateEmployee(@PathVariable Long id, @RequestBody EmployeeFinalizeRequestDto finalizeDto) {
        EmployeeResponseDto updatedEmployee = employeeService.activateEmployee(id, finalizeDto);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @PostMapping("/employees/{id}/reject-documents")
    public ResponseEntity<EmployeeResponseDto> rejectEmployeeDocuments(@PathVariable Long id, @RequestBody RejectionRequestDto rejectionDto) {
        EmployeeResponseDto updatedEmployee = employeeService.rejectDocuments(id, rejectionDto.getReason());
        return ResponseEntity.ok(updatedEmployee);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployeesForCurrentOrganization());
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @PutMapping("/employees/{id}/bank-account")
    public ResponseEntity<EmployeeResponseDto> updateEmployeeBankAccount(@PathVariable Long id, @RequestBody UpdateBankAccountDto dto) {
        return ResponseEntity.ok(employeeService.updateBankAccount(id, dto));
    }

    @PutMapping("/employees/{id}/salary-structure")
    public ResponseEntity<EmployeeResponseDto> updateEmployeeSalaryStructure(@PathVariable Long id, @RequestBody UpdateSalaryStructureDto dto) {
        return ResponseEntity.ok(employeeService.updateSalaryStructure(id, dto));
    }

    @GetMapping("/employees/eligible-for-payroll")
    public ResponseEntity<Page<EligibleEmployeeDto>> getEligibleEmployees(@PageableDefault(size = 20, sort = "firstName") Pageable pageable) {
        return ResponseEntity.ok(employeeService.getEligibleEmployeesForPayroll(pageable));
    }
    
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping("/employees/{id}/enable")
    public ResponseEntity<EmployeeResponseDto> enableEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.enableEmployee(id));
    }
    
    @PostMapping("/vendors")
    public ResponseEntity<VendorResponseDto> addVendor(@RequestBody VendorCreateRequestDto createDto) {
        return new ResponseEntity<>(vendorService.addVendor(createDto), HttpStatus.CREATED);
    }
    
    @GetMapping("/vendors")
    public ResponseEntity<Page<VendorResponseDto>> getAllVendors(Pageable pageable) {
        return ResponseEntity.ok(vendorService.getAllVendors(pageable));
    }
    
    @PostMapping(value = "/deposits/request", consumes = "multipart/form-data")
    public ResponseEntity<String> requestDeposit(@RequestPart("deposit") String depositJson, @RequestPart("file") MultipartFile file) throws IOException {
        String amountValue = "the requested amount";
        try {
            amountValue = new ObjectMapper().readTree(depositJson).get("amount").asText();
        } catch (JsonProcessingException | NullPointerException e) { /* Ignore */ }
        depositService.requestDeposit(depositJson, file);
        return new ResponseEntity<>("Deposit request for " + amountValue + " submitted successfully.", HttpStatus.CREATED);
    }

    @GetMapping("/deposits/history")
    public ResponseEntity<List<DepositResponseDto>> getMyDepositHistory(Authentication authentication) {
    	return ResponseEntity.ok(depositService.getDepositHistoryForOrganization());
	}
    
    @GetMapping("/concerns")
    public ResponseEntity<Page<ConcernResponseDto>> getAllConcerns(@RequestParam(required = false) ConcernStatus status, Pageable pageable) {
        return ResponseEntity.ok(concernService.getAllConcernsForOrganization(status, pageable));
    }

    @PostMapping("/concerns/{id}/respond")
    public ResponseEntity<String> addResponseToConcern(@PathVariable Long id, @RequestBody ConcernCommentRequestDto commentDto) {
        concernService.addResponseToConcern(id, commentDto);
        return ResponseEntity.ok("Response added successfully.");
    }

    @PutMapping("/concerns/{id}/status")
    public ResponseEntity<ConcernResponseDto> updateConcernStatus(@PathVariable Long id, @RequestBody ConcernStatusUpdateRequestDto statusUpdateDto) {
        return ResponseEntity.ok(concernService.updateConcernStatus(id, statusUpdateDto));
    }	
    
    @GetMapping("/employees/{id}/documents")
    public ResponseEntity<List<DocumentDto>> getEmployeeDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentsForEntity("Employee", id));
    }

    @GetMapping("/concerns/{id}/attachments")
    public ResponseEntity<List<DocumentDto>> getConcernAttachments(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentsForEntity("Concern", id));
    }
    
    @GetMapping("/payment-history")
    public ResponseEntity<List<PaymentHistoryDto>> getMyPaymentHistory(Authentication authentication) {
    	return ResponseEntity.ok(paymentService.getPaymentHistoryForOrganization());
    }
}