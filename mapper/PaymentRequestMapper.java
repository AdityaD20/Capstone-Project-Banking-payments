package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.PaymentRequestResponseDto;
import com.aurionpro.app.entity.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentRequestMapper {

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "organization.name", target = "organizationName")
    PaymentRequestResponseDto toDto(PaymentRequest paymentRequest);
}