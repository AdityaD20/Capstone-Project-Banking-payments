package com.aurionpro.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aurionpro.app.dto.VendorResponseDto;
import com.aurionpro.app.entity.Vendor;

@Mapper(componentModel = "spring")
public interface VendorMapper {

	@Mapping(source = "organization.id", target = "organizationId")
	VendorResponseDto toDto(Vendor vendor);
}