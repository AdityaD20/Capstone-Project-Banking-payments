package com.aurionpro.app.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.DepositResponseDto;
import com.aurionpro.app.dto.RejectionRequestDto;

public interface DepositService {
    void requestDeposit(String requestDtoJson, MultipartFile file) throws IOException;

    List<DepositResponseDto> getPendingDepositRequests();
    DepositResponseDto approveDeposit(Long depositRequestId);
    DepositResponseDto rejectDeposit(Long depositRequestId, RejectionRequestDto rejectionDto);
    
    List<DepositResponseDto> getDepositHistoryForOrganization();
}