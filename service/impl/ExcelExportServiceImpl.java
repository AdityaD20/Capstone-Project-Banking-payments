package com.aurionpro.app.service.impl;

import com.aurionpro.app.dto.TransactionReportDto;
import com.aurionpro.app.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    @Override
    public ByteArrayInputStream generateTransactionReportExcel(List<TransactionReportDto> reportData) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Transaction Report");

            // --- Corrected Header Row ---
            String[] headers = {"Transaction ID", "Transaction Date", "Payment Type", "Recipient Name", "Description", "Amount", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
            }

            // --- Corrected Data Population ---
            int rowIdx = 1;
            for (TransactionReportDto transaction : reportData) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(transaction.getTransactionId());
                row.createCell(1).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(2).setCellValue(transaction.getType().toString());
                row.createCell(3).setCellValue(transaction.getRecipientName());
                row.createCell(4).setCellValue(transaction.getDescription());
                row.createCell(5).setCellValue(transaction.getAmount().doubleValue());
                row.createCell(6).setCellValue(transaction.getStatus().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
        }
    }
}