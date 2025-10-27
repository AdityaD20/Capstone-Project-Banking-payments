package com.aurionpro.app.service;

import com.aurionpro.app.dto.PaymentRequestResponseDto;
import com.aurionpro.app.dto.RejectionRequestDto;
import java.util.List;

public interface BankAdminService {
    List<PaymentRequestResponseDto> getPendingPaymentRequests();
    PaymentRequestResponseDto approvePaymentRequest(Long paymentRequestId);
    PaymentRequestResponseDto rejectPaymentRequest(Long paymentRequestId, RejectionRequestDto rejectionDto);
}