package com.aurionpro.app.service;

import java.util.List;

import com.aurionpro.app.dto.PaymentHistoryDto;
import com.aurionpro.app.dto.PaymentRequestDto;
import com.aurionpro.app.dto.SalaryDisbursementRequestDto;

public interface PaymentService {

    void createVendorPaymentRequest(PaymentRequestDto requestDto);

	void initiateMonthlySalaryDisbursal(SalaryDisbursementRequestDto disbursementDto);
	
	List<PaymentHistoryDto> getPaymentHistoryForOrganization();
}