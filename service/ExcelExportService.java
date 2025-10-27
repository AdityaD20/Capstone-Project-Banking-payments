package com.aurionpro.app.service;

import com.aurionpro.app.dto.TransactionReportDto;
import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelExportService {
    ByteArrayInputStream generateTransactionReportExcel(List<TransactionReportDto> reportData);
}