package com.aurionpro.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.PaymentRequestDto;
import com.aurionpro.app.dto.SalaryDisbursementRequestDto;
import com.aurionpro.app.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/vendor")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> createVendorPayment(@RequestBody PaymentRequestDto requestDto) {
        paymentService.createVendorPaymentRequest(requestDto);
        return new ResponseEntity<>("Vendor payment request created successfully and is pending approval.", HttpStatus.CREATED);
    }

    @PostMapping("/salary/disburse")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> disburseSalaries(@RequestBody SalaryDisbursementRequestDto disbursementDto) {
        paymentService.initiateMonthlySalaryDisbursal(disbursementDto);
        return ResponseEntity.ok("Monthly salary disbursal request has been initiated and is pending approval.");
    }
}