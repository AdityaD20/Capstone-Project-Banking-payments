package com.aurionpro.app.service;

import com.aurionpro.app.dto.ForgotPasswordRequestDto;
import com.aurionpro.app.dto.LoginRequestDto;
import com.aurionpro.app.dto.LoginResponseDto;
import com.aurionpro.app.dto.RegistrationRequestDto;
import com.aurionpro.app.dto.ResetPasswordRequestDto;

public interface AuthService {
	void registerUser(RegistrationRequestDto registrationDto);

	LoginResponseDto loginUser(LoginRequestDto loginDto);

	void processForgotPassword(ForgotPasswordRequestDto forgotPasswordDto);

	void processResetPassword(ResetPasswordRequestDto resetPasswordDto);
}