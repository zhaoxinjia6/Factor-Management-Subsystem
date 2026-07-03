package com.factor.interfaces.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void shouldCreateLoginRequest() {
        LoginRequest request = new LoginRequest("testuser", "password123", "TRADER");

        assertEquals("testuser", request.username());
        assertEquals("password123", request.password());
        assertEquals("TRADER", request.userType());
    }

    @Test
    void shouldHandleNullValues() {
        LoginRequest request = new LoginRequest(null, null, null);

        assertNull(request.username());
        assertNull(request.password());
        assertNull(request.userType());
    }

    @Test
    void shouldHandleEmptyValues() {
        LoginRequest request = new LoginRequest("", "", "");

        assertEquals("", request.username());
        assertEquals("", request.password());
        assertEquals("", request.userType());
    }
}