package com.spendsmart.recurring;

import com.spendsmart.recurring.client.fallback.ExpenseServiceFallback;
import com.spendsmart.recurring.client.fallback.IncomeServiceFallback;
import com.spendsmart.recurring.client.fallback.NotificationServiceFallback;
import com.spendsmart.recurring.config.RabbitMQConfig;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.recurring.dto.ExpenseRequest;
import com.spendsmart.recurring.dto.IncomeRequest;
import com.spendsmart.shared.events.NotificationEvent;
import com.spendsmart.recurring.dto.NotificationRequest;
import com.spendsmart.recurring.dto.RecurringRequest;
import com.spendsmart.recurring.dto.RecurringResponse;
import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.PaymentMethod;
import com.spendsmart.recurring.entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class SupportClassesTest {

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            RecurringTransactionServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() ->
                    SpringApplication.run(RecurringTransactionServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }

    @Test
    void supportDtosAndFallbacks_ShouldExposeExpectedValues() {
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryId(1L)
                .title("Rent")
                .amount(new BigDecimal("2000"))
                .currency("INR")
                .type("EXPENSE")
                .paymentMethod(PaymentMethod.BANK)
                .date(LocalDate.now())
                .notes("Monthly rent")
                .isRecurring(true)
                .build();
        IncomeRequest incomeRequest = IncomeRequest.builder()
                .title("Salary")
                .source("SALARY")
                .amount(new BigDecimal("5000"))
                .currency("INR")
                .date(LocalDate.now())
                .notes("Payroll")
                .isRecurring(true)
                .build();
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(1L)
                .type("RECURRING_DUE")
                .severity("INFO")
                .title("Due")
                .message("Recurring transaction due")
                .relatedId(2L)
                .relatedType("RECURRING")
                .build();
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .recipientId(1L)
                .type("RECURRING_DUE")
                .severity("INFO")
                .title("Due")
                .message("Recurring transaction due")
                .relatedId(2L)
                .relatedType("RECURRING")
                .build();
        RecurringRequest recurringRequest = RecurringRequest.builder()
                .categoryId(1L)
                .title("Rent")
                .amount(new BigDecimal("2000"))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.now())
                .description("Monthly rent")
                .paymentMethod(PaymentMethod.BANK)
                .build();
        RecurringResponse recurringResponse = RecurringResponse.builder()
                .recurringId(9L)
                .userId(4L)
                .categoryId(1L)
                .title("Rent")
                .amount(new BigDecimal("2000"))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.now())
                .nextDueDate(LocalDate.now().plusMonths(1))
                .isActive(true)
                .paymentMethod(PaymentMethod.BANK)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ApiResponse<String> success = ApiResponse.success("ok", "payload");
        ApiResponse<Void> error = ApiResponse.error("down");
        ApiResponse<String> manual = ApiResponse.<String>builder().success(true).message("manual").data("payload").build();
        NotificationEvent mutableEvent = new NotificationEvent();
        mutableEvent.setRecipientId(3L);
        mutableEvent.setType("SYSTEM");
        mutableEvent.setSeverity("WARNING");
        mutableEvent.setTitle("Heads up");
        mutableEvent.setMessage("Review recurring transaction");
        mutableEvent.setRelatedId(8L);
        mutableEvent.setRelatedType("RECURRING");

        assertThat(expenseRequest.getPaymentMethod()).isEqualTo(PaymentMethod.BANK);
        assertThat(incomeRequest.getSource()).isEqualTo("SALARY");
        assertThat(event.getRecipientId()).isEqualTo(1L);
        assertThat(event.getType()).isEqualTo("RECURRING_DUE");
        assertThat(event.getSeverity()).isEqualTo("INFO");
        assertThat(event.getTitle()).isEqualTo("Due");
        assertThat(event.getMessage()).isEqualTo("Recurring transaction due");
        assertThat(event.getRelatedId()).isEqualTo(2L);
        assertThat(event.getRelatedType()).isEqualTo("RECURRING");
        assertThat(notificationRequest.getTitle()).isEqualTo("Due");
        assertThat(recurringRequest.getFrequency()).isEqualTo(Frequency.MONTHLY);
        assertThat(recurringResponse.getIsActive()).isTrue();
        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getTimestamp()).isNotNull();
        assertThat(error.isSuccess()).isFalse();
        assertThat(manual.getData()).isEqualTo("payload");
        assertThat(mutableEvent.getRecipientId()).isEqualTo(3L);
        assertThat(mutableEvent.getType()).isEqualTo("SYSTEM");
        assertThat(mutableEvent.getSeverity()).isEqualTo("WARNING");
        assertThat(mutableEvent.getTitle()).isEqualTo("Heads up");
        assertThat(mutableEvent.getMessage()).isEqualTo("Review recurring transaction");
        assertThat(mutableEvent.getRelatedId()).isEqualTo(8L);
        assertThat(mutableEvent.getRelatedType()).isEqualTo("RECURRING");

        assertThat(new ExpenseServiceFallback().createExpense(expenseRequest, "token", 1L).getMessage()).contains("Expense Service");
        assertThat(new IncomeServiceFallback().createIncome(incomeRequest, "token", 1L).getMessage()).contains("Income Service");
        new NotificationServiceFallback().sendNotification(notificationRequest);
    }

    @Test
    void rabbitTemplate_ShouldUseJsonConverter() {
        RabbitMQConfig config = new RabbitMQConfig();
        RabbitTemplate template = config.rabbitTemplate(mock(ConnectionFactory.class));

        assertThat(config.notificationExchange().getName()).isEqualTo(RabbitMQConfig.NOTIFICATION_EXCHANGE);
        assertThat(config.converter()).isInstanceOf(Jackson2JsonMessageConverter.class);
        assertThat(template.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
