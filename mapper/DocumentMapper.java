package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.DocumentDto;
import com.aurionpro.app.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
	
    @Mapping(source = "displayName", target = "displayName")
    DocumentDto toDto(Document document);
}