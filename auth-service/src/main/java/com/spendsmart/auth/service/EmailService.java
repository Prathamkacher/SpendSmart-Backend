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
import java.awt.Color;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final String UTF_8 = StandardCharsets.UTF_8.name();

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
}
