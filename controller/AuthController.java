package com.aurionpro.app.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin; // 1. ADD THIS IMPORT
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.ForgotPasswordRequestDto;
import com.aurionpro.app.dto.LoginRequestDto;
import com.aurionpro.app.dto.LoginResponseDto;
import com.aurionpro.app.dto.OrganizationRegistrationRequestDto;
import com.aurionpro.app.dto.ResetPasswordRequestDto;
import com.aurionpro.app.service.AuthService;
import com.aurionpro.app.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // 2. ADD THIS ANNOTATION
public class AuthController {

    private final OrganizationService organizationService;
    private final AuthService authService;

    @PostMapping("/register/organization")
    public ResponseEntity<Map<String, String>> registerOrganization(@RequestBody OrganizationRegistrationRequestDto requestDto) {
        organizationService.registerOrganization(requestDto);
        String successMessage = "Organization registered successfully! Awaiting initial approval.";
        return new ResponseEntity<>(Map.of("message", successMessage), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto loginDto) {
        LoginResponseDto response = authService.loginUser(loginDto);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequestDto requestDto) {
        authService.processForgotPassword(requestDto);
        // We always return a generic success message to prevent email enumeration
        String successMessage = "If an account with that email exists, a password reset link has been sent.";
        return ResponseEntity.ok(Map.of("message", successMessage));
    }

    // NEW ENDPOINT for setting the new password
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequestDto requestDto) {
        authService.processResetPassword(requestDto);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now log in."));
    }
}