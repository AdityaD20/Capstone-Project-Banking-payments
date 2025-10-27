package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.VendorStatus;

import lombok.Data;

@Data
public class VendorResponseDto {
	private Long id;
	private String name;
	private String contactEmail;
	private String contactPhone;
	private VendorStatus status;
	private Long organizationId;
}