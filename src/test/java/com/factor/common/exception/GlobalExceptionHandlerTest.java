package com.factor.common.exception;

import com.factor.common.api.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器测试
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleBusinessException() {
        BusinessException ex = new BusinessException("用户不存在");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleBusinessException(ex);
        assertEquals(400, resp.getStatusCode().value());
        assertFalse(Objects.requireNonNull(resp.getBody()).success());
        assertEquals("用户不存在", resp.getBody().message());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("参数不合法");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleIllegalArgumentException(ex);
        assertEquals(400, resp.getStatusCode().value());
        assertFalse(Objects.requireNonNull(resp.getBody()).success());
        assertEquals("参数不合法", resp.getBody().message());
    }

    @Test
    void testHandleUnexpectedException() {
        Exception ex = new RuntimeException("数据库连接失败");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleUnexpectedException(ex);
        assertEquals(500, resp.getStatusCode().value());
        assertFalse(Objects.requireNonNull(resp.getBody()).success());
        assertTrue(resp.getBody().message().contains("数据库连接失败"));
    }

    @Test
    void testHandleUnexpectedExceptionWithNullMessage() {
        Exception ex = new NullPointerException();
        ResponseEntity<ApiResponse<Void>> resp = handler.handleUnexpectedException(ex);
        assertEquals(500, resp.getStatusCode().value());
        assertTrue(Objects.requireNonNull(resp.getBody()).message().contains("NullPointerException"));
    }

    @Test
    void testHandleUnexpectedExceptionWithCause() {
        Exception cause = new RuntimeException("底层异常");
        Exception ex = new RuntimeException(null, cause);
        ResponseEntity<ApiResponse<Void>> resp = handler.handleUnexpectedException(ex);
        assertEquals(500, resp.getStatusCode().value());
        assertTrue(Objects.requireNonNull(resp.getBody()).message().contains("底层异常"));
    }

    @Test
    void testApiResponseFailMessage() {
        ApiResponse<Void> resp = ApiResponse.fail("自定义错误");
        assertFalse(resp.success());
        assertEquals("自定义错误", resp.message());
    }

    @Test
    void testApiResponseOk() {
        ApiResponse<String> resp = ApiResponse.ok("成功数据");
        assertTrue(resp.success());
        assertEquals("成功数据", resp.data());
    }
}
