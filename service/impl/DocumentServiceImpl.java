package com.aurionpro.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.DocumentDto;
import com.aurionpro.app.entity.Document;
import com.aurionpro.app.exception.FileUploadException;
import com.aurionpro.app.mapper.DocumentMapper;
import com.aurionpro.app.repository.DocumentRepository;
import com.aurionpro.app.service.DocumentService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

	private final Cloudinary cloudinary;
	private final DocumentRepository documentRepository;
	private final DocumentMapper documentMapper;

	@Override
	@Retryable(value = { IOException.class }, // Only retry on specific network/IO errors
			maxAttempts = 3, backoff = @Backoff(delay = 2000))
	public Document uploadFile(MultipartFile file, String entityName, Long entityId) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
        String fileUrl = (String) uploadResult.get("secure_url");
        Document document = new Document();
        document.setUrl(fileUrl);
        document.setType(file.getContentType());
        document.setDisplayName(file.getOriginalFilename());
        document.setEntityName(entityName);
        document.setEntityId(entityId);
        return documentRepository.save(document);
    }

	@Recover
	public Document recover(IOException e, MultipartFile file, String entityName, Long entityId) {
		throw new FileUploadException("Could not upload file after multiple attempts. Please try again later.", e);
	}

	@Override
	public List<DocumentDto> getDocumentsForEntity(String entityName, Long entityId) {
		return documentRepository.findByEntityNameAndEntityId(entityName, entityId).stream().map(documentMapper::toDto)
				.collect(Collectors.toList());
	}
}