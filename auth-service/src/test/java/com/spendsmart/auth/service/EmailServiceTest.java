package com.spendsmart.auth.service;

import com.spendsmart.auth.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("sendOtpEmail() - should send HTML email with OTP")
    void sendOtpEmail_ShouldSendEmail() {
        emailService.sendOtpEmail("test@example.com", "123456");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendWelcomeEmail() - should send HTML welcome email")
    void sendWelcomeEmail_ShouldSendEmail() {
        emailService.sendWelcomeEmail("test@example.com", "Test User");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendAdminNotificationNewUser() - should send HTML alert to admin")
    void sendAdminNotificationNewUser_ShouldSendEmail() {
        User user = User.builder()
                .fullName("New User")
                .email("new@example.com")
                .build();
        emailService.sendAdminNotificationNewUser("admin@example.com", user);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendPremiumActivationEmail() - should send HTML email with PDF invoice")
    void sendPremiumActivationEmail_ShouldSendEmail() {
        emailService.sendPremiumActivationEmail("test@example.com", "Test User", "PRO", 1499.0);
        verify(mailSender).send(any(MimeMessage.class));
    }
}
