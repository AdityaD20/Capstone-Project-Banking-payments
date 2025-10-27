package com.aurionpro.app.service;

import java.math.BigDecimal;

public interface EmailService {
	void sendCredentialsEmail(String toEmail);

	void sendActivationEmail(String toEmail);

	void sendInitialRejectionEmail(String toEmail, String reason);

	void sendDocumentRejectionEmail(String toEmail, String reason);

	void sendEmployeeCredentialsEmail(String toEmail, String temporaryPassword);

	void sendEmployeeActivationEmail(String toEmail);

	void sendEmployeeDocumentRejectionEmail(String toEmail, String reason);

	void sendVendorCredentialsEmail(String toEmail, String temporaryPassword);

	void sendPaymentApprovedEmail(String toEmail, Long paymentRequestId, BigDecimal amount);

	void sendPaymentRejectedEmail(String toEmail, Long paymentRequestId, String reason);

	void sendDepositApprovalEmail(String toEmail, BigDecimal amount);

	void sendDepositRejectionEmail(String toEmail, String reason);

	void sendPasswordResetEmail(String toEmail, String token);
}