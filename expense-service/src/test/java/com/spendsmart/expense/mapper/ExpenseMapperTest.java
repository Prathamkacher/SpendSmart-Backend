package com.spendsmart.expense.mapper;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.entity.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseMapperTest {

    private final ExpenseMapper mapper = Mappers.getMapper(ExpenseMapper.class);

    @Test
    void toEntity() {
        ExpenseRequest request = new ExpenseRequest();
        request.setCategoryId(2L);
        request.setTitle("Lunch");
        request.setAmount(new BigDecimal("15"));
        request.setCurrency("USD");
        request.setType(ExpenseType.EXPENSE);
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setDate(LocalDate.now());
        request.setNotes("Notes");
        request.setReceiptUrl("url");
        request.setIsRecurring(false);

        Expense entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(request.getCategoryId(), entity.getCategoryId());
        assertEquals(request.getTitle(), entity.getTitle());
        assertEquals(request.getAmount(), entity.getAmount());
        assertEquals(request.getCurrency(), entity.getCurrency());
        assertEquals(request.getType(), entity.getType());
        assertEquals(request.getPaymentMethod(), entity.getPaymentMethod());
        assertEquals(request.getDate(), entity.getDate());
        assertEquals(request.getNotes(), entity.getNotes());
        assertEquals(request.getReceiptUrl(), entity.getReceiptUrl());
        assertEquals(request.getIsRecurring(), entity.getIsRecurring());
    }

    @Test
    void toEntity_Null() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse() {
        Expense entity = new Expense();
        entity.setExpenseId(1L);
        entity.setUserId(2L);
        entity.setCategoryId(3L);
        entity.setTitle("Lunch");
        entity.setAmount(new BigDecimal("15"));
        entity.setCurrency("USD");
        entity.setType(ExpenseType.EXPENSE);
        entity.setPaymentMethod(PaymentMethod.CASH);
        entity.setDate(LocalDate.now());
        entity.setNotes("Notes");
        entity.setReceiptUrl("url");
        entity.setIsRecurring(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        ExpenseResponse dto = mapper.toResponse(entity);

        assertNotNull(dto);
        assertEquals(entity.getExpenseId(), dto.getExpenseId());
        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getCategoryId(), dto.getCategoryId());
        assertEquals(entity.getTitle(), dto.getTitle());
        assertEquals(entity.getAmount(), dto.getAmount());
        assertEquals(entity.getCurrency(), dto.getCurrency());
        assertEquals(entity.getType(), dto.getType());
        assertEquals(entity.getPaymentMethod(), dto.getPaymentMethod());
        assertEquals(entity.getDate(), dto.getDate());
        assertEquals(entity.getNotes(), dto.getNotes());
        assertEquals(entity.getReceiptUrl(), dto.getReceiptUrl());
        assertEquals(entity.getIsRecurring(), dto.getIsRecurring());
        assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void toResponse_Null() {
        assertNull(mapper.toResponse(null));
    }
    
    @Test
    void updateEntityFromRequest() {
        Expense entity = new Expense();
        entity.setTitle("Old");
        
        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("New");
        request.setAmount(new BigDecimal("100"));
        
        mapper.updateEntityFromRequest(request, entity);
        
        assertEquals("New", entity.getTitle());
        assertEquals(new BigDecimal("100"), entity.getAmount());
    }
    
    @Test
    void updateEntityFromRequest_Null() {
        Expense entity = new Expense();
        entity.setTitle("Old");
        mapper.updateEntityFromRequest(null, entity);
        assertEquals("Old", entity.getTitle());
    }
}
