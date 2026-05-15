package com.spendsmart.notification.service;

import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.client.AuthClient;
import com.spendsmart.notification.dto.UserProfileResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AuthClient authClient;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Async
    public void sendCriticalAlertEmail(Notification notification) {
        try {
            UserProfileResponse user = authClient.getUserById(notification.getRecipientId());
            if (user == null || user.getEmail() == null) {
                log.warn("Cannot send email: User or email not found for recipientId={}", notification.getRecipientId());
                return;
            }
            String email = user.getEmail();
            log.info("Sending professional HTML critical alert to {}: {}", email, notification.getTitle());
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("CRITICAL ALERT: " + notification.getTitle());
            
            String htmlContent = "<!DOCTYPE html><html><head><style>" +
                "body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #fff1f2; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); border-top: 6px solid #e11d48; }" +
                ".header { background-color: #ffffff; padding: 24px; text-align: center; border-bottom: 1px solid #f1f5f9; }" +
                ".header h1 { margin: 0; font-size: 24px; color: #e11d48; font-weight: 800; }" +
                ".content { padding: 40px 32px; color: #334155; line-height: 1.6; font-size: 16px; }" +
                ".alert-title { color: #0f172a; font-size: 20px; font-weight: 700; margin-bottom: 16px; }" +
                ".footer { background-color: #f1f5f9; padding: 24px; text-align: center; color: #64748b; font-size: 13px; }" +
                ".warning-box { background-color: #fff1f2; border: 1px solid #fda4af; padding: 16px; border-radius: 12px; color: #9f1239; font-weight: 500; margin: 24px 0; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h1>SpendSmart Security</h1></div>" +
                "<div class='content'>" +
                "<div class='alert-title'>" + notification.getTitle() + "</div>" +
                "<p>Hello,</p>" +
                "<p>We are notifying you about a critical event regarding your SpendSmart account:</p>" +
                "<div class='warning-box'>" + notification.getMessage() + "</div>" +
                "<p>Please take appropriate action or log in to your dashboard to review the details.</p>" +
                "<br><p>Stay secure,<br><strong>SpendSmart Team</strong></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; " + Year.now().getValue() + " SpendSmart. All rights reserved.</p>" +
                "</div></div></body></html>";
                
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Critical HTML alert successfully sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send critical HTML alert", e);
        }
    }

    @Async
    public void sendReceiptEmail(Notification notification) {
        try {
            UserProfileResponse user = authClient.getUserById(notification.getRecipientId());
            if (user == null || user.getEmail() == null) return;
            
            String email = user.getEmail();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Payment Success: Welcome to SpendSmart Pro!");
            
            String htmlContent = "<!DOCTYPE html><html><head><style>" +
                "body { font-family: 'Inter', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 0; }" +
                ".wrapper { padding: 40px 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 24px; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1); }" +
                ".header { background: linear-gradient(135deg, #10b981, #059669); padding: 40px; text-align: center; color: white; }" +
                ".success-icon { background: rgba(255,255,255,0.2); width: 64px; height: 64px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 20px; font-size: 32px; }" +
                ".content { padding: 40px; color: #1e293b; }" +
                ".receipt-box { background: #f1f5f9; border-radius: 16px; padding: 24px; margin: 24px 0; border: 1px dashed #cbd5e1; }" +
                ".receipt-row { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px; }" +
                ".receipt-label { color: #64748b; font-weight: 500; }" +
                ".receipt-value { color: #0f172a; font-weight: 700; }" +
                ".total-row { border-top: 1px solid #cbd5e1; padding-top: 12px; margin-top: 12px; font-size: 18px; }" +
                ".footer { text-align: center; padding: 32px; color: #94a3b8; font-size: 12px; }" +
                ".btn { display: inline-block; padding: 14px 28px; background: #10b981; color: white; text-decoration: none; border-radius: 12px; font-weight: 700; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='wrapper'><div class='container'>" +
                "<div class='header'><div class='success-icon'>✓</div><h1>Congratulations!</h1><p>You are now a SpendSmart Pro member</p></div>" +
                "<div class='content'>" +
                "<p>Hello " + user.getFullName() + ",</p>" +
                "<p>Success! Your payment was processed successfully. You now have full access to all premium features including advanced analytics and custom reports.</p>" +
                "<div class='receipt-box'>" +
                "<div class='receipt-row'><span class='receipt-label'>Plan</span><span class='receipt-value'>SpendSmart PRO</span></div>" +
                "<div class='receipt-row'><span class='receipt-label'>Transaction ID</span><span class='receipt-value'>#" + notification.getRelatedId() + "</span></div>" +
                "<div class='receipt-row'><span class='receipt-label'>Status</span><span class='receipt-value' style='color:#10b981'>PAID</span></div>" +
                "<div class='total-row receipt-row'><span class='receipt-label'>Total Paid</span><span class='receipt-value'>" + notification.getMessage() + "</span></div>" +
                "</div>" +
                "<p>We're excited to help you take control of your financial future.</p>" +
                "<center><a href='" + frontendUrl + "/dashboard' class='btn'>Explore Pro Features</a></center>" +
                "</div>" +
                "<div class='footer'><p>&copy; " + Year.now().getValue() + " SpendSmart. Premium Financial Management.</p></div>" +
                "</div></div></body></html>";
                
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Premium receipt email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send premium receipt email", e);
        }
    }
}
