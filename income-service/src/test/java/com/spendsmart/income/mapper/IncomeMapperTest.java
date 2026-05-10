package com.spendsmart.income.mapper;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.dto.IncomeResponse;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.entity.RecurrencePeriod;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IncomeMapperTest {

    private final IncomeMapper mapper = Mappers.getMapper(IncomeMapper.class);

    @Test
    void toEntity() {
        IncomeRequest request = new IncomeRequest();
        request.setCategoryId(1L);
        request.setTitle("Salary");
        request.setAmount(new BigDecimal("5000"));
        request.setCurrency("USD");
        request.setSource(IncomeSource.SALARY);
        request.setDate(LocalDate.now());
        request.setNotes("Monthly salary");
        request.setIsRecurring(true);
        request.setRecurrencePeriod(RecurrencePeriod.MONTHLY);

        Income entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(request.getCategoryId(), entity.getCategoryId());
        assertEquals(request.getTitle(), entity.getTitle());
        assertEquals(request.getAmount(), entity.getAmount());
        assertEquals(request.getCurrency(), entity.getCurrency());
        assertEquals(request.getSource(), entity.getSource());
        assertEquals(request.getDate(), entity.getDate());
        assertEquals(request.getNotes(), entity.getNotes());
        assertEquals(request.getIsRecurring(), entity.getIsRecurring());
        assertEquals(request.getRecurrencePeriod(), entity.getRecurrencePeriod());
    }

    @Test
    void toEntity_Null() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse() {
        Income entity = new Income();
        entity.setIncomeId(10L);
        entity.setUserId(2L);
        entity.setCategoryId(1L);
        entity.setTitle("Salary");
        entity.setAmount(new BigDecimal("5000"));
        entity.setCurrency("USD");
        entity.setSource(IncomeSource.SALARY);
        entity.setDate(LocalDate.now());
        entity.setNotes("Monthly salary");
        entity.setIsRecurring(true);
        entity.setRecurrencePeriod(RecurrencePeriod.MONTHLY);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        IncomeResponse dto = mapper.toResponse(entity);

        assertNotNull(dto);
        assertEquals(entity.getIncomeId(), dto.getIncomeId());
        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getCategoryId(), dto.getCategoryId());
        assertEquals(entity.getTitle(), dto.getTitle());
        assertEquals(entity.getAmount(), dto.getAmount());
        assertEquals(entity.getCurrency(), dto.getCurrency());
        assertEquals(entity.getSource(), dto.getSource());
        assertEquals(entity.getDate(), dto.getDate());
        assertEquals(entity.getNotes(), dto.getNotes());
        assertEquals(entity.getIsRecurring(), dto.getIsRecurring());
        assertEquals(entity.getRecurrencePeriod(), dto.getRecurrencePeriod());
        assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void toResponse_Null() {
        assertNull(mapper.toResponse(null));
    }
    
    @Test
    void updateEntity() {
        Income entity = new Income();
        entity.setTitle("Old");
        
        IncomeRequest request = new IncomeRequest();
        request.setTitle("New");
        request.setAmount(new BigDecimal("100"));
        
        mapper.updateEntity(entity, request);
        
        assertEquals("New", entity.getTitle());
        assertEquals(new BigDecimal("100"), entity.getAmount());
    }
    
    @Test
    void updateEntity_Null() {
        Income entity = new Income();
        entity.setTitle("Old");
        mapper.updateEntity(entity, null);
        assertEquals("Old", entity.getTitle());
    }
}
