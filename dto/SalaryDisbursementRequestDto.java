package com.aurionpro.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class SalaryDisbursementRequestDto {
    private List<PaymentRequestItemDto> employeesToPay;
    
}