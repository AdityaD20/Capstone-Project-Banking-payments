package com.aurionpro.app.service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

import com.aurionpro.app.dto.PaySlipDetailDto;
import com.aurionpro.app.dto.TransactionReportDto;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.ServletOutputStream;

public interface PdfExportService {
	
	public void generatePaySlipPdf(PaySlipDetailDto payslipDto, OutputStream outputStream) throws DocumentException, IOException;

	void generateTransactionReportPdf(List<TransactionReportDto> transactions, LocalDate from, LocalDate to,
			ServletOutputStream outputStream) throws DocumentException, IOException;
}