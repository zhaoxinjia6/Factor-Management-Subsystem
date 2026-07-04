package com.factor.interfaces.rest.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FactorQueryRequestTest {

    @Test
    void shouldCreateFactorQueryRequest() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        FactorQueryRequest request = new FactorQueryRequest("000001", "f1", start, end, 1, 10);

        assertEquals("000001", request.fundCode());
        assertEquals("f1", request.factorId());
        assertEquals(start, request.startDate());
        assertEquals(end, request.endDate());
        assertEquals(1, request.page());
        assertEquals(10, request.size());
    }

    @Test
    void shouldHandleNullDates() {
        FactorQueryRequest request = new FactorQueryRequest("000001", "f1", null, null, 0, 0);

        assertNull(request.startDate());
        assertNull(request.endDate());
    }

    @Test
    void shouldHandleDefaultValues() {
        FactorQueryRequest request = new FactorQueryRequest(null, null, null, null, 0, 0);

        assertNull(request.fundCode());
        assertNull(request.factorId());
        assertEquals(0, request.page());
        assertEquals(0, request.size());
    }
}