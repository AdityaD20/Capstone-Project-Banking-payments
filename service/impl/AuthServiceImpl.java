package com.aurionpro.app.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.dto.ForgotPasswordRequestDto;
import com.aurionpro.app.dto.LoginRequestDto;
import com.aurionpro.app.dto.LoginResponseDto;
import com.aurionpro.app.dto.RegistrationRequestDto;
import com.aurionpro.app.dto.ResetPasswordRequestDto;
import com.aurionpro.app.entity.user.PasswordResetToken;
import com.aurionpro.app.entity.user.Role;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.InvalidStateException;
import com.aurionpro.app.repository.PasswordResetTokenRepository;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.security.JwtTokenProvider;
import com.aurionpro.app.service.AuthService;
import com.aurionpro.app.service.CaptchaService;
import com.aurionpro.app.service.EmailService;
import com.aurionpro.app.util.ValidationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final CaptchaService captchaService;
	private final PasswordResetTokenRepository tokenRepository;
	private final EmailService emailService;

	@Override
	public void registerUser(RegistrationRequestDto registrationDto) {
		if (!captchaService.validateCaptcha(registrationDto.getCaptchaResponse())) {
			throw new InvalidStateException("reCAPTCHA validation failed. Please try again.");
		}

		// Check if user already exists
		if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
			throw new RuntimeException("Error: Email is already in use!");
		}

		// Create new user's account
		User user = new User();
		user.setEmail(registrationDto.getEmail());
		user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

		// Assign role
		Role userRole = roleRepository.findByName(registrationDto.getRole())
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		user.setRoles(roles);

		userRepository.save(user);
	}

	@Override
    public LoginResponseDto loginUser(LoginRequestDto loginDto) {
    	if (!captchaService.validateCaptcha(loginDto.getCaptchaResponse())) {
            throw new InvalidStateException("reCAPTCHA validation failed. Please try again.");
        }
    	
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
           
            String token = jwtTokenProvider.generateToken(authentication);
           
            User user = userRepository.findByEmail(loginDto.getEmail()).get();
            Set<String> roles = user.getRoles().stream()
                                    .map(role -> role.getName().toString())
                                    .collect(Collectors.toSet());

            return new LoginResponseDto(token, user.getEmail(), roles);

        } catch (BadCredentialsException ex) {
            throw new InvalidStateException("Invalid email or password provided.");
        } catch (DisabledException ex) { // 2. ADD THIS CATCH BLOCK
            throw new InvalidStateException("This account has been disabled. Please contact support.");
        }
    }


	@Override
	@Transactional
	public void processForgotPassword(ForgotPasswordRequestDto forgotPasswordDto) {
		Optional<User> userOptional = userRepository.findByEmail(forgotPasswordDto.getEmail());

		// To prevent user enumeration attacks, we don't reveal if the user was found.
		if (userOptional.isEmpty()) {
			log.warn("Password reset requested for non-existent email: {}", forgotPasswordDto.getEmail());
			return; // Silently exit
		}

		User user = userOptional.get();
		String token = UUID.randomUUID().toString();

		// If a token already exists for this user, update it. Otherwise, create a new
		// one.
		PasswordResetToken resetToken = tokenRepository.findByUser(user).orElse(new PasswordResetToken(token, user));

		resetToken.setToken(token);
		resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

		tokenRepository.save(resetToken);
		emailService.sendPasswordResetEmail(user.getEmail(), token);
	}

	@Override
	@Transactional
	public void processResetPassword(ResetPasswordRequestDto resetPasswordDto) {
		PasswordResetToken token = tokenRepository.findByToken(resetPasswordDto.getToken())
				.orElseThrow(() -> new InvalidStateException("Invalid password reset token."));

		if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
			tokenRepository.delete(token);
			throw new InvalidStateException("Password reset token has expired.");
		}

		if (!ValidationUtils.isPasswordStrong(resetPasswordDto.getNewPassword())) {
			throw new InvalidStateException("New password does not meet complexity requirements.");
		}

		User user = token.getUser();
		user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
		user.setPasswordChangeRequired(false); // The password has been changed
		userRepository.save(user);

		// Invalidate the token after use
		tokenRepository.delete(token);
	}
}