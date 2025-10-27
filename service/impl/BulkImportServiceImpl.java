package com.aurionpro.app.service.impl;

import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.service.BulkImportService;
import com.aurionpro.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportServiceImpl implements BulkImportService {

    @Qualifier("asyncJobLauncher")
    private final JobLauncher jobLauncher;
    private final Job importEmployeesJob; // Assuming this bean name from BatchConfig
    private final UserService userService;
    private final OrganizationRepository organizationRepository;

    @Override
    public void runBulkImportJob(MultipartFile file) throws Exception {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", orgUser.getEmail()));

        // Save the file to a temporary location
        Path tempDir = Files.createTempDirectory("employee-batch-");
        File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
        file.transferTo(tempFile);
        log.info("Uploaded batch file saved to temporary path: {}", tempFile.getAbsolutePath());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("filePath", tempFile.getAbsolutePath())
                .addLong("organizationId", organization.getId())
                .addString("jobId", String.valueOf(System.currentTimeMillis())) // Ensures job is always runnable
                .toJobParameters();
        
        jobLauncher.run(importEmployeesJob, jobParameters);
    }
}