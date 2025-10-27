package com.aurionpro.app.service;

import org.springframework.web.multipart.MultipartFile;

public interface BulkImportService {
    void runBulkImportJob(MultipartFile file) throws Exception;
}