package com.spendsmart.auth.service;

import com.spendsmart.auth.entity.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.awt.Color;
import java.nio.charset.StandardCharsets;

/**
 * Service for sending system emails.
 * Handles welcome emails, OTPs, admin notifications, and account status updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    private String buildHtmlTemplate(String title, String bodyContent) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }" +
                ".header { background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 32px 24px; text-align: center; color: #ffffff; }" +
                ".header h1 { margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }" +
                ".content { padding: 40px 32px; color: #334155; line-height: 1.6; font-size: 16px; }" +
                ".content h2 { color: #0f172a; margin-top: 0; }" +
                ".footer { background-color: #f1f5f9; padding: 24px; text-align: center; color: #64748b; font-size: 13px; }" +
                ".btn { display: inline-block; background-color: #10b981; color: #ffffff; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: bold; margin-top: 16px; }" +
                ".otp-box { background-color: #f1f5f9; border: 2px dashed #cbd5e1; padding: 16px; text-align: center; font-size: 32px; font-weight: 800; letter-spacing: 4px; color: #0f172a; border-radius: 12px; margin: 24px 0; }" +
                ".premium-box { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); padding: 24px; border-radius: 16px; color: white; text-align: center; margin: 24px 0; }" +
                ".premium-box h3 { margin: 0 0 8px 0; font-size: 20px; font-weight: 800; }" +
                ".premium-box p { margin: 0; opacity: 0.9; font-weight: 600; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h1>SpendSmart</h1></div>" +
                "<div class='content'>" +
                "<h2>" + title + "</h2>" +
                bodyContent +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; " + java.time.Year.now().getValue() + " SpendSmart. All rights reserved.</p>" +
                "<p>This is an automated message, please do not reply.</p>" +
                "</div></div></body></html>";
    }

    /**
     * Sends an OTP email for password reset.
     *
     * @param toEmail Recipient email.
     * @param otp The One-Time Password.
     */
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("Sending OTP email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            
            helper.setTo(toEmail);
            helper.setSubject("SpendSmart - Password Reset OTP");
            
            String bodyContent = "<p>Hello,</p>" +
                    "<p>We received a request to reset your SpendSmart password.</p>" +
                    "<p>Your One-Time Password (OTP) is:</p>" +
                    "<div class='otp-box'>" + otp + "</div>" +
                    "<p>This OTP is valid for 10 minutes. If you did not request a password reset, please ignore this email and your password will remain unchanged.</p>" +
                    "<br><p>Best regards,<br><strong>The SpendSmart Team</strong></p>";
                    
            helper.setText(buildHtmlTemplate("Password Reset", bodyContent), true);
            mailSender.send(message);
            log.info("OTP email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", toEmail, e);
            throw new MailSendException("Failed to send email", e);
        }
    }

    /**
     * Sends a welcome email to a newly registered user.
     *
     * @param toEmail Recipient email.
     * @param fullName User's full name.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        log.info("Sending welcome email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            
            helper.setTo(toEmail);
            helper.setSubject("Welcome to SpendSmart!");
            
            String bodyContent = "<p>Hello <strong>" + fullName + "</strong>,</p>" +
                    "<p>Welcome to SpendSmart! We are thrilled to have you on board.</p>" +
                    "<p>SpendSmart is designed to help you track your expenses, manage your income, and achieve your financial goals with ease.</p>" +
                    "<p>If you have any questions or need help getting started, feel free to reach out to us.</p>" +
                    "<br><p>Happy budgeting!<br><strong>The SpendSmart Team</strong></p>";
                    
            helper.setText(buildHtmlTemplate("Welcome Aboard!", bodyContent), true);
            mailSender.send(message);
            log.info("Welcome email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", toEmail, e);
        }
    }
    
    /**
     * Notifies the admin about a new user registration.
     *
     * @param adminEmail Admin's email address.
     * @param user The new user entity.
     */
    @Async
    public void sendAdminNotificationNewUser(String adminEmail, User user) {
        log.info("Sending new user notification to admin: {}", adminEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            
            helper.setTo(adminEmail);
            helper.setSubject("Admin Alert: New User Registration");
            
            String bodyContent = "<p>Hello Admin,</p>" +
                    "<p>A new user has just registered on SpendSmart.</p>" +
                    "<table style='width:100%; border-collapse: collapse; margin-top: 16px;'>" +
                    "<tr><td style='padding:8px; border-bottom:1px solid #e2e8f0; font-weight:bold;'>Full Name:</td><td style='padding:8px; border-bottom:1px solid #e2e8f0;'>" + user.getFullName() + "</td></tr>" +
                    "<tr><td style='padding:8px; border-bottom:1px solid #e2e8f0; font-weight:bold;'>Email:</td><td style='padding:8px; border-bottom:1px solid #e2e8f0;'>" + user.getEmail() + "</td></tr>" +
                    "<tr><td style='padding:8px; border-bottom:1px solid #e2e8f0; font-weight:bold;'>Registration Time:</td><td style='padding:8px; border-bottom:1px solid #e2e8f0;'>" + java.time.LocalDateTime.now().toString() + "</td></tr>" +
                    "</table>" +
                    "<br><p>Best regards,<br><strong>SpendSmart System</strong></p>";
                    
            helper.setText(buildHtmlTemplate("New User Registration", bodyContent), true);
            mailSender.send(message);
            log.info("Admin notification email successfully sent to {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send admin notification email to {}", adminEmail, e);
        }
    }

    /**
     * Sends an email confirming premium plan activation with an attached PDF invoice.
     *
     * @param toEmail Recipient email.
     * @param fullName User's full name.
     * @param planName Name of the activated plan.
     * @param amount Amount paid.
     */
    @Async
    public void sendPremiumActivationEmail(String toEmail, String fullName, String planName, Double amount) {
        log.info("Sending premium activation email with invoice to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            
            helper.setTo(toEmail);
            helper.setSubject("SpendSmart Pro - Welcome to Premium!");
            
            String bodyContent = "<p>Hello <strong>" + fullName + "</strong>,</p>" +
                    "<p>Your upgrade to <strong>SpendSmart Pro</strong> was successful! You now have full access to all advanced features.</p>" +
                    "<div class='premium-box'>" +
                    "<h3>PREMIUM ACTIVATED</h3>" +
                    "<p>" + planName + " Plan</p>" +
                    "</div>" +
                    "<p><strong>What's new for you:</strong></p>" +
                    "<ul>" +
                    "<li>Unlimited Expense Tracking</li>" +
                    "<li>Advanced Analytics & Trends</li>" +
                    "<li>Custom Reports & Exports</li>" +
                    "<li>Priority Support</li>" +
                    "</ul>" +
                    "<p>We have attached your official payment invoice to this email for your records.</p>" +
                    "<br><p>Thank you for choosing SpendSmart!<br><strong>The SpendSmart Team</strong></p>";
                    
            helper.setText(buildHtmlTemplate("Premium Upgrade Successful", bodyContent), true);
            
            // Generate and attach PDF
            byte[] pdfBytes = generateInvoicePdf(fullName, planName, amount);
            helper.addAttachment("SpendSmart_Invoice.pdf", new org.springframework.core.io.ByteArrayResource(pdfBytes));
            
            mailSender.send(message);
            log.info("Premium activation email with invoice sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send premium email to {}", toEmail, e);
        }
    }

    private byte[] generateInvoicePdf(String fullName, String planName, Double amount) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Header
            com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 24, Color.BLACK);
            com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("SPENDSMART INVOICE", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);
            
            // Details
            com.lowagie.text.Font normalFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 12);
            document.add(new com.lowagie.text.Paragraph("Invoice Date: " + java.time.LocalDate.now().toString(), normalFont));
            document.add(new com.lowagie.text.Paragraph("Invoice Number: INV-" + System.currentTimeMillis(), normalFont));
            document.add(new com.lowagie.text.Paragraph("\n", normalFont));
            
            document.add(new com.lowagie.text.Paragraph("Bill To:", com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 12)));
            document.add(new com.lowagie.text.Paragraph(fullName, normalFont));
            document.add(new com.lowagie.text.Paragraph("\n\n", normalFont));
            
            // Table
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(2);
            table.setWidthPercentage(100);
            
            table.addCell("Description");
            table.addCell("Amount");
            
            table.addCell("SpendSmart Premium Subscription (" + planName + ")");
            table.addCell("INR " + String.format("%.2f", amount));
            
            document.add(table);
            
            // Total
            com.lowagie.text.Paragraph total = new com.lowagie.text.Paragraph("\nTotal Amount Paid: INR " + String.format("%.2f", amount), com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 14));
            total.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            document.add(total);
            
            // Footer
            com.lowagie.text.Paragraph footer = new com.lowagie.text.Paragraph("\n\nThank you for your business!", com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            return new byte[0];
        }
    }

    /**
     * Notifies a user about a change in their account role.
     *
     * @param toEmail Recipient email.
     * @param fullName User's full name.
     * @param newRole The new role assigned.
     */
    @Async
    public void sendRoleUpdateEmail(String toEmail, String fullName, String newRole) {
        log.info("Sending role update email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            
            helper.setTo(toEmail);
            helper.setSubject("SpendSmart Account - Role Update");
            
            String roleColor = newRole.equals("ADMIN") ? "#fbbf24" : "#94a3b8";
            
            String bodyContent = "<p>Hello <strong>" + fullName + "</strong>,</p>" +
                    "<p>Your account privileges on SpendSmart have been updated by the platform administrator.</p>" +
                    "<div style='background: #1e293b; border-radius: 16px; padding: 32px; text-align: center; margin: 24px 0; border: 1px solid #334155;'>" +
                    "<div style='font-size: 48px; margin-bottom: 16px;'> " + (newRole.equals("ADMIN") ? "🛡️" : "👤") + " </div>" +
                    "<p style='color: #94a3b8; font-size: 14px; text-transform: uppercase; letter-spacing: 0.1em; margin: 0;'>New Account Status</p>" +
                    "<h3 style='color: " + roleColor + "; font-size: 32px; margin: 8px 0; font-weight: 800;'>" + newRole + "</h3>" +
                    "</div>" +
                    "<p>As an <strong>" + newRole + "</strong>, your access levels have been adjusted accordingly. Please log in to explore your " + (newRole.equals("ADMIN") ? "administrative" : "new") + " dashboard.</p>" +
                    "<a href='" + frontendUrl + "' class='btn'>Log In to SpendSmart</a>" +
                    "<br><br><p>Best regards,<br><strong>The SpendSmart Admin Team</strong></p>";
                    
            helper.setText(buildHtmlTemplate("Account Privilege Update", bodyContent), true);
            mailSender.send(message);
            log.info("Role update email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send role update email to {}", toEmail, e);
        }
    }

    /**
     * Notifies a user that their account has been suspended.
     *
     * @param toEmail Recipient email.
     * @param fullName User's full name.
     */
    @Async
    public void sendAccountSuspendedEmail(String toEmail, String fullName) {
        log.info("Sending account suspension email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);

            helper.setTo(toEmail);
            helper.setSubject("SpendSmart Account - Access Suspended");

            String bodyContent = "<p>Hello <strong>" + fullName + "</strong>,</p>" +
                    "<p>We are writing to inform you that your SpendSmart account has been <strong>temporarily suspended</strong> by the platform administrator.</p>" +
                    "<div style='background: #1e293b; border-radius: 16px; padding: 32px; text-align: center; margin: 24px 0; border: 1px solid #334155;'>" +
                    "<div style='font-size: 48px; margin-bottom: 16px;'>🚫</div>" +
                    "<p style='color: #94a3b8; font-size: 14px; text-transform: uppercase; letter-spacing: 0.1em; margin: 0;'>Account Status</p>" +
                    "<h3 style='color: #f43f5e; font-size: 32px; margin: 8px 0; font-weight: 800;'>SUSPENDED</h3>" +
                    "</div>" +
                    "<p>While your account is suspended:</p>" +
                    "<ul>" +
                    "<li>You will not be able to log in to your account</li>" +
                    "<li>All scheduled transactions are paused</li>" +
                    "<li>Your data remains safe and intact</li>" +
                    "</ul>" +
                    "<p>If you believe this was done in error, please contact our support team for assistance.</p>" +
                    "<br><p>Best regards,<br><strong>The SpendSmart Admin Team</strong></p>";

            helper.setText(buildHtmlTemplate("Account Suspended", bodyContent), true);
            mailSender.send(message);
            log.info("Account suspension email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send suspension email to {}", toEmail, e);
        }
    }

    /**
     * Notifies a user that their account has been reactivated.
     *
     * @param toEmail Recipient email.
     * @param fullName User's full name.
     */
    @Async
    public void sendAccountActivatedEmail(String toEmail, String fullName) {
        log.info("Sending account reactivation email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);

            helper.setTo(toEmail);
            helper.setSubject("SpendSmart Account - Access Restored");

            String bodyContent = "<p>Hello <strong>" + fullName + "</strong>,</p>" +
                    "<p>Great news! Your SpendSmart account has been <strong>reactivated</strong> by the platform administrator.</p>" +
                    "<div style='background: #1e293b; border-radius: 16px; padding: 32px; text-align: center; margin: 24px 0; border: 1px solid #334155;'>" +
                    "<div style='font-size: 48px; margin-bottom: 16px;'>✅</div>" +
                    "<p style='color: #94a3b8; font-size: 14px; text-transform: uppercase; letter-spacing: 0.1em; margin: 0;'>Account Status</p>" +
                    "<h3 style='color: #10b981; font-size: 32px; margin: 8px 0; font-weight: 800;'>ACTIVE</h3>" +
                    "</div>" +
                    "<p>Your account is now fully operational. You can:</p>" +
                    "<ul>" +
                    "<li>Log in and access your dashboard</li>" +
                    "<li>Resume tracking expenses and income</li>" +
                    "<li>All your previous data is preserved</li>" +
                    "</ul>" +
                    "<a href='" + frontendUrl + "' class='btn'>Log In to SpendSmart</a>" +
                    "<br><br><p>Welcome back!<br><strong>The SpendSmart Admin Team</strong></p>";

            helper.setText(buildHtmlTemplate("Account Reactivated", bodyContent), true);
            mailSender.send(message);
            log.info("Account reactivation email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send reactivation email to {}", toEmail, e);
        }
    }

    /**
     * Notifies a user that their account has been deleted.
     *
     * @param toEmail Recipient email.
     * @param fullName User's full name.
     */
    @Async
    public void sendAccountDeletedEmail(String toEmail, String fullName) {
        log.info("Sending account deletion email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);

            helper.setTo(toEmail);
            helper.setSubject("SpendSmart Account - Permanently Removed");

            String bodyContent = "<p>Hello <strong>" + fullName + "</strong>,</p>" +
                    "<p>We are writing to confirm that your SpendSmart account has been <strong>permanently deleted</strong> by the platform administrator.</p>" +
                    "<div style='background: #1e293b; border-radius: 16px; padding: 32px; text-align: center; margin: 24px 0; border: 1px solid #334155;'>" +
                    "<div style='font-size: 48px; margin-bottom: 16px;'>🗑️</div>" +
                    "<p style='color: #94a3b8; font-size: 14px; text-transform: uppercase; letter-spacing: 0.1em; margin: 0;'>Account Status</p>" +
                    "<h3 style='color: #ef4444; font-size: 32px; margin: 8px 0; font-weight: 800;'>DELETED</h3>" +
                    "</div>" +
                    "<p>This action is irreversible. All associated data including:</p>" +
                    "<ul>" +
                    "<li>Your profile and account settings</li>" +
                    "<li>Expense and income records</li>" +
                    "<li>Budget configurations</li>" +
                    "<li>Recurring transaction schedules</li>" +
                    "</ul>" +
                    "<p>...have been permanently removed from our system.</p>" +
                    "<p>If you believe this was done in error, please contact our support team immediately.</p>" +
                    "<br><p>Regards,<br><strong>The SpendSmart Admin Team</strong></p>";

            helper.setText(buildHtmlTemplate("Account Deleted", bodyContent), true);
            mailSender.send(message);
            log.info("Account deletion email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send deletion email to {}", toEmail, e);
        }
    }
}
