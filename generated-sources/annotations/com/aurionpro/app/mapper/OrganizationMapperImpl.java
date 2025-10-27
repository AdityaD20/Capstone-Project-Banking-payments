package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.OrganizationDto;
import com.aurionpro.app.entity.BankAccountDetails;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.user.User;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class OrganizationMapperImpl implements OrganizationMapper {

    @Override
    public OrganizationDto toDto(Organization organization) {
        if ( organization == null ) {
            return null;
        }

        OrganizationDto organizationDto = new OrganizationDto();

        organizationDto.setEmail( organizationUserEmail( organization ) );
        organizationDto.setBalance( organizationBankAccountBalance( organization ) );
        organizationDto.setRejectionReason( organization.getRejectionReason() );
        organizationDto.setCreatedAt( organization.getCreatedAt() );
        organizationDto.setId( organization.getId() );
        organizationDto.setName( organization.getName() );
        organizationDto.setStatus( organization.getStatus() );

        return organizationDto;
    }

    private String organizationUserEmail(Organization organization) {
        if ( organization == null ) {
            return null;
        }
        User user = organization.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    private BigDecimal organizationBankAccountBalance(Organization organization) {
        if ( organization == null ) {
            return null;
        }
        BankAccountDetails bankAccount = organization.getBankAccount();
        if ( bankAccount == null ) {
            return null;
        }
        BigDecimal balance = bankAccount.getBalance();
        if ( balance == null ) {
            return null;
        }
        return balance;
    }
}
