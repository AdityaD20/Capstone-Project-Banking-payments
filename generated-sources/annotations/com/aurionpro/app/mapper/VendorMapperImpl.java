package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.VendorResponseDto;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.Vendor;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class VendorMapperImpl implements VendorMapper {

    @Override
    public VendorResponseDto toDto(Vendor vendor) {
        if ( vendor == null ) {
            return null;
        }

        VendorResponseDto vendorResponseDto = new VendorResponseDto();

        vendorResponseDto.setOrganizationId( vendorOrganizationId( vendor ) );
        vendorResponseDto.setContactEmail( vendor.getContactEmail() );
        vendorResponseDto.setContactPhone( vendor.getContactPhone() );
        vendorResponseDto.setId( vendor.getId() );
        vendorResponseDto.setName( vendor.getName() );
        vendorResponseDto.setStatus( vendor.getStatus() );

        return vendorResponseDto;
    }

    private Long vendorOrganizationId(Vendor vendor) {
        if ( vendor == null ) {
            return null;
        }
        Organization organization = vendor.getOrganization();
        if ( organization == null ) {
            return null;
        }
        Long id = organization.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
