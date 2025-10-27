package com.aurionpro.app.service.impl;

import com.aurionpro.app.service.BankAdminJobLauncherService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BankAdminJobLauncherServiceImpl implements BankAdminJobLauncherService {

    private final JobLauncher jobLauncher;
    private final Job salaryDisbursementJob;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // Runs outside of any transaction
    public void launchPayslipGenerationJob(Long paymentRequestId) {
        try {
            jobLauncher.run(salaryDisbursementJob, new JobParametersBuilder()
                    .addLong("paymentRequestId", paymentRequestId)
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters());
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to launch the payslip generation batch job for request ID: " + paymentRequestId);
            e.printStackTrace();
        }
    }
}