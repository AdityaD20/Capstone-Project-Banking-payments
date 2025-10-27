package com.aurionpro.app.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.DocumentDto;
import com.aurionpro.app.entity.Document;

public interface DocumentService {
	Document uploadFile(MultipartFile file, String entityName, Long entityId) throws IOException;
	    
    List<DocumentDto> getDocumentsForEntity(String entityName, Long entityId);

}