package com.aurionpro.app.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.ConcernCommentRequestDto;
import com.aurionpro.app.dto.ConcernResponseDto;
import com.aurionpro.app.dto.ConcernStatusUpdateRequestDto;
import com.aurionpro.app.entity.enums.ConcernStatus;

public interface ConcernService {
    ConcernResponseDto createConcern(String description, MultipartFile attachment) throws IOException;
    Page<ConcernResponseDto> getMyConcerns(Pageable pageable);

    Page<ConcernResponseDto> getAllConcernsForOrganization(ConcernStatus status, Pageable pageable);
    void addResponseToConcern(Long concernId, ConcernCommentRequestDto commentDto);
    ConcernResponseDto updateConcernStatus(Long concernId, ConcernStatusUpdateRequestDto statusUpdateDto);
    
    List<ConcernResponseDto> getResponsesForMyConcern(Long concernId);
}