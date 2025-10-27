package com.aurionpro.app.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.dto.ChangePasswordDto;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.service.AuditLogService;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.util.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database: " + email));
    }
    
    @Override
    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) {
        User currentUser = getCurrentUser();

        // 1. Verify old password
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), currentUser.getPassword())) {
            throw new InvalidStateException("Incorrect old password.");
        }
        
        if (!ValidationUtils.isPasswordStrong(changePasswordDto.getNewPassword())) {
            throw new InvalidStateException("New password does not meet complexity requirements. It must be at least 8 characters long and include an uppercase letter, a lowercase letter, a number, and a special symbol.");
        }

        currentUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        currentUser.setPasswordChangeRequired(false);

        User updatedUser = userRepository.save(currentUser);

        auditLogService.logAction(
                updatedUser,
                ActionType.UPDATE,
                "User",
                updatedUser.getId(),
                "User changed their password successfully."
        );
    }
}