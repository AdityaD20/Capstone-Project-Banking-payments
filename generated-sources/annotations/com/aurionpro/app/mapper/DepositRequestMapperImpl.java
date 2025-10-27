package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.DepositResponseDto;
import com.aurionpro.app.entity.DepositRequest;
import com.aurionpro.app.entity.Organization;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class DepositRequestMapperImpl implements DepositRequestMapper {

    @Override
    public DepositResponseDto toDto(DepositRequest depositRequest) {
        if ( depositRequest == null ) {
            return null;
        }

        DepositResponseDto depositResponseDto = new DepositResponseDto();

        depositResponseDto.setOrganizationName( depositRequestOrganizationName( depositRequest ) );
        depositResponseDto.setAmount( depositRequest.getAmount() );
        depositResponseDto.setCreatedAt( depositRequest.getCreatedAt() );
        depositResponseDto.setDescription( depositRequest.getDescription() );
        depositResponseDto.setId( depositRequest.getId() );
        depositResponseDto.setRejectionReason( depositRequest.getRejectionReason() );
        depositResponseDto.setStatus( depositRequest.getStatus() );

        return depositResponseDto;
    }

    private String depositRequestOrganizationName(DepositRequest depositRequest) {
        if ( depositRequest == null ) {
            return null;
        }
        Organization organization = depositRequest.getOrganization();
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
