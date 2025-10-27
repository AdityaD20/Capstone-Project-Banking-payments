package com.aurionpro.app.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aurionpro.app.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${app.frontend.url:http://localhost:4200}") // Default to localhost if not set
    private String frontendUrl;

    @Override
    public void sendCredentialsEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Registration is Approved - Next Steps");
        message.setText("Congratulations! Your initial registration has been approved. Please log in to upload your verification documents to complete the process.");
        mailSender.send(message);
    }

    @Override
    public void sendActivationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Account is Now Active!");
        message.setText("Welcome! Your account has been fully activated. You can now access all features of the Payment and Payroll Management System.");
        mailSender.send(message);
    }

    @Override
    public void sendInitialRejectionEmail(String toEmail, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Update on Your Registration");
        message.setText("We regret to inform you that your registration could not be approved at this time.\n Reason: " + reason+ "\nIf you believe this is an error or would like to discuss your application further, please contact our business support team at support@bank.com");
        mailSender.send(message);
    }

    @Override
    public void sendDocumentRejectionEmail(String toEmail, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Action Required: Please Re-upload Your Documents");
        message.setText("There was an issue with the documents you provided. Reason: " + reason + ". Please log in to your dashboard to upload the correct documents.");
        mailSender.send(message);
    }
    
    
    @Override
    public void sendEmployeeCredentialsEmail(String toEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Account has been Created - Action Required");
        message.setText("Welcome! Your employee account has been created.\n\n" +
                        "Your username is: " + toEmail + "\n" +
                        "Your secure, randomly generated temporary password is: " + temporaryPassword + "\n\n" +
                        "Please log in to your dashboard to upload documents. You will be required to change your password immediately after your first login.");
        mailSender.send(message);
    }

    @Override
    public void sendEmployeeActivationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Employee Account is Now Active!");
        message.setText("Congratulations! Your documents have been approved and your account is now fully active.");
        mailSender.send(message);
    }
    
    @Override
    public void sendEmployeeDocumentRejectionEmail(String toEmail, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Action Required: Update on Your Onboarding Documents");
        message.setText("There was an issue with the documents you submitted.\n Reason: " + reason 
                        + ". \nPlease log in to your dashboard to re-upload the correct documents.");
        mailSender.send(message);
    }
    
    
    @Override
    public void sendDepositApprovalEmail(String toEmail, BigDecimal amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Deposit Request has been Approved");
        message.setText(String.format(
            "Hello,\n\nYour deposit request has been approved.\n\n" +
            "Amount: %,.2f\n\n" +
            "The funds have been credited to your account balance.\n\nThank you.",
            amount
        ));
        mailSender.send(message);
    }

    @Override
    public void sendDepositRejectionEmail(String toEmail, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Update on Your Deposit Request");
        message.setText(String.format(
            "Hello,\n\nThere was an issue with your recent deposit request.\n\n" +
            "Status: REJECTED\n" +
            "Reason: %s\n\n" +
            "Please review the reason and submit a new request if necessary.",
            reason
        ));
        mailSender.send(message);
    }
    
    
    @Override
    public void sendPaymentApprovedEmail(String toEmail, Long paymentRequestId, BigDecimal amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Payment Request has been Approved");
        message.setText(String.format(
            "Hello,\n\nGood news! Your payment request (#%d) has been approved and processed.\n\n" +
            "Amount: %,.2f\n\n" +
            "The funds have been debited from your account.\n\nThank you.",
            paymentRequestId, amount
        ));
        mailSender.send(message);
    }

    @Override
    public void sendPaymentRejectedEmail(String toEmail, Long paymentRequestId, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Action Required: Update on your Payment Request");
        message.setText(String.format(
            "Hello,\n\nThere was an issue with your payment request (#%d).\n\n" +
            "Status: REJECTED\n" +
            "Reason: %s\n\n" +
            "Please log in to your dashboard for more details. No funds have been debited.",
            paymentRequestId, reason
        ));
        mailSender.send(message);
    }
    
    @Override
	public void sendVendorCredentialsEmail(String toEmail, String temporaryPassword) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Your Vendor Account Has Been Created");
		message.setText("Welcome! An organization has created a vendor account for you on our platform.\n\n"
				+ "Your account is now active and ready to receive payments.\n\n" + "Your username is: " + toEmail
				+ "\n" + "Your temporary password is: " + temporaryPassword + "\n\n"
				+ "For your security, you will be required to change this password upon your first login.");
		mailSender.send(message);
	}
    
    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Password Reset Request");
        message.setText("Hello,\n\nYou have requested to reset your password.\n\n"
                      + "Click the link below to set a new password:\n" + resetUrl + "\n\n"
                      + "If you did not request a password reset, please ignore this email.\n"
                      + "This link will expire in 1 hour.");
        mailSender.send(message);
    }
    
}