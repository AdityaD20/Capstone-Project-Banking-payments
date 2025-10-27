package com.aurionpro.app.dto;

import java.util.Set;
import lombok.Data;

@Data
public class UserDetailDto {
	private Long id;
	private String email;
	private boolean passwordChangeRequired;
	private Set<String> roles;
}