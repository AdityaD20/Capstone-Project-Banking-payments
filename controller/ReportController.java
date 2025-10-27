package com.aurionpro.app.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.TransactionReportDto;
import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.service.ExcelExportService;
import com.aurionpro.app.service.PdfExportService;
import com.aurionpro.app.service.ReportService;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizations/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZATION')")
public class ReportController {

	private final ReportService reportService;
	private final PdfExportService pdfExportService;
	private final ExcelExportService excelExportService;

	@GetMapping("/view")
	public ResponseEntity<Page<TransactionReportDto>> viewReport(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) PaymentType type, Pageable pageable) {
		Page<TransactionReportDto> reportData = reportService.getTransactionReport(from, to, type, pageable);
		return ResponseEntity.ok(reportData);
	}

	@GetMapping("/download-pdf")
	public void downloadReportAsPdf(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) PaymentType type, HttpServletResponse response)
			throws IOException, DocumentException {

		List<TransactionReportDto> reportData = reportService.getTransactionReportForDownload(from, to, type);

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\"transaction-report.pdf\"");

		pdfExportService.generateTransactionReportPdf(reportData, from, to, response.getOutputStream());
	}
	
	@GetMapping("/download-excel")
    public ResponseEntity<InputStreamResource> downloadReportAsExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PaymentType type) throws IOException {

        // 1. Fetch the same data as the PDF report
        List<TransactionReportDto> reportData = reportService.getTransactionReportForDownload(from, to, type);

        // 2. Generate the Excel file in memory
        ByteArrayInputStream bis = excelExportService.generateTransactionReportExcel(reportData);

        // 3. Set the correct headers for an Excel file download
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"transaction-report.xlsx\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}