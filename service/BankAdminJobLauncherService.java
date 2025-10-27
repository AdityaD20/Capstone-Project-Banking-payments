package com.aurionpro.app.service;

public interface BankAdminJobLauncherService {
    void launchPayslipGenerationJob(Long paymentRequestId);
}