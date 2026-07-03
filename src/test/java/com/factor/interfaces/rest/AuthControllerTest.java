package com.factor.interfaces.rest;

import com.factor.application.auth.AuthApplicationService;
import com.factor.common.api.ApiResponse;
import com.factor.domain.auth.*;
import com.factor.interfaces.rest.dto.LoginRequest;
import com.factor.interfaces.rest.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证控制器测试
 */
class AuthControllerTest {

    private AuthController controller;
    private AuthApplicationServiceStub authService;

    @BeforeEach
    void setUp() {
        authService = new AuthApplicationServiceStub();
        controller = new AuthController(authService);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("admin", "admin123", "SYSTEM_ADMIN");
        ApiResponse<LoginVO> response = controller.login(request);

        assertTrue(response.success());
        assertEquals("OK", response.message());
        assertNotNull(response.data());
        assertNotNull(response.data().token());
    }

    @Test
    void testLoginReturnToken() {
        LoginRequest request = new LoginRequest("admin", "admin123", "SYSTEM_ADMIN");
        ApiResponse<LoginVO> response = controller.login(request);

        assertNotNull(response.data().token());
    }

    @Test
    void testLoginReturnAccountInfo() {
        LoginRequest request = new LoginRequest("admin", "admin123", "SYSTEM_ADMIN");
        ApiResponse<LoginVO> response = controller.login(request);

        LoginVO.AccountVO account = response.data().account();
        assertNotNull(account);
        assertEquals("admin", account.username());
        assertEquals("系统超级管理员", account.displayName());
    }

    @Test
    void testLoginReturnRoleInfo() {
        LoginRequest request = new LoginRequest("admin", "admin123", "SYSTEM_ADMIN");
        ApiResponse<LoginVO> response = controller.login(request);

        LoginVO.RoleVO role = response.data().role();
        assertNotNull(role);
        assertEquals("SYSTEM_ADMIN", role.code());
    }

    @Test
    void testLoginReturnMenus() {
        LoginRequest request = new LoginRequest("admin", "admin123", "SYSTEM_ADMIN");
        ApiResponse<LoginVO> response = controller.login(request);

        assertNotNull(response.data().menus());
        assertTrue(response.data().menus().size() > 0);
    }

    @Test
    void testTraderLogin() {
        authService.setupTrader();
        LoginRequest request = new LoginRequest("trader", "trader123", "TRADER");
        ApiResponse<LoginVO> response = controller.login(request);

        assertTrue(response.success());
        assertEquals("交易员", response.data().account().displayName());
    }

    @Test
    void testCustomerLogin() {
        authService.setupCustomer();
        LoginRequest request = new LoginRequest("customer", "customer123", "CUSTOMER");
        ApiResponse<LoginVO> response = controller.login(request);

        assertTrue(response.success());
        assertEquals("客户", response.data().account().displayName());
    }

    /**
     * AuthApplicationService 测试桩
     */
    private static class AuthApplicationServiceStub implements AuthApplicationService {
        private boolean traderMode = false;
        private boolean customerMode = false;

        void setupTrader() { traderMode = true; customerMode = false; }
        void setupCustomer() { traderMode = false; customerMode = true; }

        @Override
        public AuthSession login(String username, String password, String userType) {
            UserType type = UserType.valueOf(userType);
            Account account;
            Role role;
            List<MenuItem> menus;

            if (traderMode) {
                account = new Account("a2", "trader", "trader123", "交易员", UserType.TRADER, "r2", "t1", true);
                role = new Role("r2", "TRADER", "交易员", UserType.TRADER, RoleScope.BUSINESS, List.of(), true);
                menus = List.of(
                        new MenuItem("t1", null, "交易单管理", "/trade-orders", "TradeOrderPage", "trade", null, List.of())
                );
            } else if (customerMode) {
                account = new Account("a3", "customer", "customer123", "客户", UserType.CUSTOMER, "r3", "t1", true);
                role = new Role("r3", "CUSTOMER", "客户", UserType.CUSTOMER, RoleScope.BUSINESS, List.of(), true);
                menus = List.of(
                        new MenuItem("c1", null, "我的产品", "/my-products", "MyProductsPage", "product", null, List.of())
                );
            } else {
                account = new Account("a1", "admin", "admin123", "系统超级管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
                role = new Role("r1", "SYSTEM_ADMIN", "系统超级管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM, List.of(), true);
                menus = List.of(
                        new MenuItem("m1", null, "租户管理", "/tenants", "TenantPage", "tenant", null, List.of())
                );
            }
            return new AuthSession("token-" + username + "-" + userType, account, role, menus);
        }
    }
}
