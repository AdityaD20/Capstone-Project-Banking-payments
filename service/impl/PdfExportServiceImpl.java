package com.aurionpro.app.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.aurionpro.app.dto.PaySlipDetailDto;
import com.aurionpro.app.dto.TransactionReportDto;
import com.aurionpro.app.service.PdfExportService;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.ServletOutputStream;
import lombok.RequiredArgsConstructor;

/**
 * A centralized service for generating PDF documents from HTML templates. It
 * uses Thymeleaf to process the templates and Flying Saucer (xhtmlrenderer) to
 * convert the resulting HTML into a PDF.
 */
@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {
	
	private final TemplateEngine templateEngine;

	/**
	 * Generates a detailed PDF payslip for a single employee.
	 *
	 * @param payslipDto   The DTO containing all the detailed information for the
	 *                     payslip.
	 * @param outputStream The output stream where the generated PDF will be
	 *                     written.
	 * @throws DocumentException if an error occurs during PDF creation.
	 * @throws IOException 
	 */
	
	@Override
	public void generatePaySlipPdf(PaySlipDetailDto payslipDto, OutputStream outputStream) throws DocumentException, IOException {
		// 1. Create a Thymeleaf context for the payslip data.
		// The key "payslip" must match the variable name used in the HTML template.
		Context context = new Context();
		context.setVariable("payslip", payslipDto);

		// 2. Process the HTML template ('payslip_template.html').
		String htmlContent = templateEngine.process("payslip_template", context);
		
		// 3. Render the final HTML to a PDF.
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocumentFromString(htmlContent);
		renderer.layout();
		renderer.createPDF(outputStream);
	}
	
	@Override
	public void generateTransactionReportPdf(List<TransactionReportDto> transactions, LocalDate from, LocalDate to,
			ServletOutputStream outputStream) throws DocumentException, IOException {
		Context context = new Context();
        context.setVariable("transactions", transactions);
        context.setVariable("from", from);
        context.setVariable("to", to);

        String htmlContent = templateEngine.process("transaction_report_pdf", context);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
	}
}