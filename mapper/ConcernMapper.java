package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.ConcernResponseDto;
import com.aurionpro.app.entity.Concern;
import com.aurionpro.app.entity.ConcernResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConcernMapper {

    @Mapping(target = "employeeName", expression = "java(concern.getEmployee().getFirstName() + \" \" + concern.getEmployee().getLastName())")
    ConcernResponseDto toDto(Concern concern);

    // This new method reuses the existing DTO for comments/responses
    @Mapping(source = "author.email", target = "employeeName")
    @Mapping(source = "responseText", target = "description")
    ConcernResponseDto concernResponseToDto(ConcernResponse concernResponse);
}