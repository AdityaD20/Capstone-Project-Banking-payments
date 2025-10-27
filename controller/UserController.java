package com.aurionpro.app.controller;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.ChangePasswordDto;
import com.aurionpro.app.dto.UserDetailDto;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

	private final UserService userService;

	@PostMapping("/change-password")
	@PreAuthorize("isAuthenticated()") // This ensures any logged-in user can access it
	public ResponseEntity<String> changeUserPassword(@RequestBody ChangePasswordDto changePasswordDto) {
		userService.changePassword(changePasswordDto);
		return ResponseEntity.ok("Password changed successfully.");
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserDetailDto> getCurrentUserDetails() {
		User currentUser = userService.getCurrentUser();
		UserDetailDto dto = new UserDetailDto();
		dto.setId(currentUser.getId());
		dto.setEmail(currentUser.getEmail());
		dto.setPasswordChangeRequired(currentUser.isPasswordChangeRequired());
		dto.setRoles(
				currentUser.getRoles().stream().map(role -> role.getName().toString()).collect(Collectors.toSet()));

		return ResponseEntity.ok(dto);
	}

}