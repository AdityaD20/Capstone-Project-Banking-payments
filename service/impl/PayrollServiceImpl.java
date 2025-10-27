package com.aurionpro.app.service.impl;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.aurionpro.app.entity.PaySlip;
import com.aurionpro.app.service.PayrollService;

@Service
public class PayrollServiceImpl implements PayrollService {

	@Override
	 public BigDecimal calculateTotalEarnings(PaySlip paySlip) {
        return Stream.of(
            paySlip.getBasicSalary(),
            paySlip.getHra(),
            paySlip.getDearnessAllowance(),
            paySlip.getOtherAllowances()
        )
        .filter(Objects::nonNull) 
        .reduce(BigDecimal.ZERO, BigDecimal::add); 
    }

	@Override
    public BigDecimal calculateTotalDeductions(PaySlip paySlip) {
        return Stream.of(
            paySlip.getProvidentFund()
        )
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}