package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.DocumentDto;
import com.aurionpro.app.entity.Document;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T19:02:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public DocumentDto toDto(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentDto documentDto = new DocumentDto();

        documentDto.setDisplayName( document.getDisplayName() );
        documentDto.setId( document.getId() );
        documentDto.setType( document.getType() );
        documentDto.setUrl( document.getUrl() );

        return documentDto;
    }
}
