package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.PaymentRequestResponseDto;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.PaymentRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class PaymentRequestMapperImpl implements PaymentRequestMapper {

    @Override
    public PaymentRequestResponseDto toDto(PaymentRequest paymentRequest) {
        if ( paymentRequest == null ) {
            return null;
        }

        PaymentRequestResponseDto paymentRequestResponseDto = new PaymentRequestResponseDto();

        paymentRequestResponseDto.setOrganizationId( paymentRequestOrganizationId( paymentRequest ) );
        paymentRequestResponseDto.setOrganizationName( paymentRequestOrganizationName( paymentRequest ) );
        paymentRequestResponseDto.setAmount( paymentRequest.getAmount() );
        paymentRequestResponseDto.setCreatedAt( paymentRequest.getCreatedAt() );
        paymentRequestResponseDto.setDescription( paymentRequest.getDescription() );
        paymentRequestResponseDto.setId( paymentRequest.getId() );
        paymentRequestResponseDto.setRejectionReason( paymentRequest.getRejectionReason() );
        paymentRequestResponseDto.setStatus( paymentRequest.getStatus() );
        paymentRequestResponseDto.setType( paymentRequest.getType() );

        return paymentRequestResponseDto;
    }

    private Long paymentRequestOrganizationId(PaymentRequest paymentRequest) {
        if ( paymentRequest == null ) {
            return null;
        }
        Organization organization = paymentRequest.getOrganization();
        if ( organization == null ) {
            return null;
        }
        Long id = organization.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String paymentRequestOrganizationName(PaymentRequest paymentRequest) {
        if ( paymentRequest == null ) {
            return null;
        }
        Organization organization = paymentRequest.getOrganization();
        if ( organization == null ) {
            return null;
        }
        String name = organization.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
