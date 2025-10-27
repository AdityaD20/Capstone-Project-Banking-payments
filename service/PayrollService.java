package com.aurionpro.app.service;

import java.math.BigDecimal;

import org.mapstruct.Named;

import com.aurionpro.app.entity.PaySlip;

public interface PayrollService {

	@Named("calculateTotalEarnings")
	BigDecimal calculateTotalEarnings(PaySlip paySlip);

	@Named("calculateTotalDeductions")
	BigDecimal calculateTotalDeductions(PaySlip paySlip);
}