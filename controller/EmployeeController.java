package com.aurionpro.app.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.ConcernResponseDto;
import com.aurionpro.app.dto.EmployeeResponseDto;
import com.aurionpro.app.dto.PaySlipSummaryDto;
import com.aurionpro.app.entity.ConcernResponse;
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.exception.FileUploadException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.ConcernMapper;
import com.aurionpro.app.mapper.EmployeeMapper;
import com.aurionpro.app.repository.ConcernResponseRepository;
import com.aurionpro.app.repository.EmployeeRepository;
import com.aurionpro.app.service.ConcernService;
import com.aurionpro.app.service.EmployeeService;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ConcernService concernService;
  

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponseDto> getMyEmployeeDetails(Authentication authentication) {
    	return ResponseEntity.ok(employeeService.getMyEmployeeDetails(authentication));
    }

    @PostMapping(value = "/me/documents/upload"  , consumes = "multipart/form-data")
    public ResponseEntity<EmployeeResponseDto> uploadDocuments(@RequestParam("files") List<MultipartFile> files, Authentication authentication) {
        String email = authentication.getName();
        EmployeeResponseDto updatedEmployee;
		try {
			updatedEmployee = employeeService.submitDocuments(email, files);
		} catch (IOException e) {
			throw new FileUploadException("Failed to upload documents. Please try again.", e);
		}
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @GetMapping("/me/payslips")
    public ResponseEntity<Page<PaySlipSummaryDto>> getMyPaySlipHistory(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getMyPaySlipHistory(pageable));
    }
    
    @GetMapping("/me/payslips/{paySlipId}/download-pdf")
    public void downloadMyPaySlip(@PathVariable Long paySlipId, HttpServletResponse response) throws DocumentException, IOException {
        employeeService.downloadMyPaySlip(paySlipId, response);
    }
    
    @PostMapping(value = "/me/concerns", consumes = "multipart/form-data")
    public ResponseEntity<ConcernResponseDto> createConcern(@RequestPart("description") String description,
                                                            @RequestPart(value = "attachment", required = false) MultipartFile attachment)  {
        ConcernResponseDto newConcern;
		try {
			newConcern = concernService.createConcern(description, attachment);
		} catch (IOException e) {
			throw new FileUploadException("Failed to upload documents. Please try again.", e);
		}
        return new ResponseEntity<>(newConcern, HttpStatus.CREATED);
    }

    @GetMapping("/me/concerns")
    public ResponseEntity<Page<ConcernResponseDto>> getMyConcernHistory(Pageable pageable) {
        return ResponseEntity.ok(concernService.getMyConcerns(pageable));
    }

    @GetMapping("/me/concerns/{id}/responses")
    public ResponseEntity<List<ConcernResponseDto>> getConcernResponses(@PathVariable Long id, Authentication authentication) {
    	return ResponseEntity.ok(concernService.getResponsesForMyConcern(id));
    }
}