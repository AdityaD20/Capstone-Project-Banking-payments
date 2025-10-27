package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.ConcernResponseDto;
import com.aurionpro.app.entity.Concern;
import com.aurionpro.app.entity.ConcernResponse;
import com.aurionpro.app.entity.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class ConcernMapperImpl implements ConcernMapper {

    @Override
    public ConcernResponseDto toDto(Concern concern) {
        if ( concern == null ) {
            return null;
        }

        ConcernResponseDto concernResponseDto = new ConcernResponseDto();

        concernResponseDto.setCreatedAt( concern.getCreatedAt() );
        concernResponseDto.setDescription( concern.getDescription() );
        concernResponseDto.setId( concern.getId() );
        concernResponseDto.setStatus( concern.getStatus() );

        concernResponseDto.setEmployeeName( concern.getEmployee().getFirstName() + " " + concern.getEmployee().getLastName() );

        return concernResponseDto;
    }

    @Override
    public ConcernResponseDto concernResponseToDto(ConcernResponse concernResponse) {
        if ( concernResponse == null ) {
            return null;
        }

        ConcernResponseDto concernResponseDto = new ConcernResponseDto();

        concernResponseDto.setEmployeeName( concernResponseAuthorEmail( concernResponse ) );
        concernResponseDto.setDescription( concernResponse.getResponseText() );
        concernResponseDto.setCreatedAt( concernResponse.getCreatedAt() );
        concernResponseDto.setId( concernResponse.getId() );

        return concernResponseDto;
    }

    private String concernResponseAuthorEmail(ConcernResponse concernResponse) {
        if ( concernResponse == null ) {
            return null;
        }
        User author = concernResponse.getAuthor();
        if ( author == null ) {
            return null;
        }
        String email = author.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
