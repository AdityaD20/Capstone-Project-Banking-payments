package com.aurionpro.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aurionpro.app.dto.TransactionReportDto;
import com.aurionpro.app.entity.enums.PaymentType;

public interface ReportService {
	Page<TransactionReportDto> getTransactionReport(LocalDate from, LocalDate to, PaymentType type, Pageable pageable);

	List<TransactionReportDto> getTransactionReportForDownload(LocalDate from, LocalDate to, PaymentType type);
}