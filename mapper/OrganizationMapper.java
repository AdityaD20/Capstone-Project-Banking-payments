package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.OrganizationDto;
import com.aurionpro.app.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "bankAccount.balance", target = "balance")
    @Mapping(source = "rejectionReason", target = "rejectionReason")
    OrganizationDto toDto(Organization organization);
}