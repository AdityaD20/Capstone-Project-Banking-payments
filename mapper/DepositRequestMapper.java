package com.aurionpro.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aurionpro.app.dto.DepositResponseDto;
import com.aurionpro.app.entity.DepositRequest;

@Mapper(componentModel = "spring")
public interface DepositRequestMapper {

    @Mapping(source = "organization.name", target = "organizationName")
    DepositResponseDto toDto(DepositRequest depositRequest);
}