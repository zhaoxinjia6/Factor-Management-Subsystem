package com.factor.domain.auth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证会话领域模型测试
 */
class AuthSessionTest {

    @Test
    void testCreateAuthSession() {
        Account account = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Role role = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.values()), true);
        List<MenuItem> menus = List.of(
                new MenuItem("m1", null, "租户管理", "/tenants", "TenantPage", "tenant", PermissionCode.TENANT_MANAGE, List.of())
        );

        AuthSession session = new AuthSession("token-admin-SYSTEM_ADMIN", account, role, menus);

        assertEquals("token-admin-SYSTEM_ADMIN", session.token());
        assertEquals(account, session.account());
        assertEquals(role, session.role());
        assertEquals(1, session.menus().size());
    }

    @Test
    void testAuthSessionWithEmptyMenus() {
        Account account = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Role role = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.values()), true);

        AuthSession session = new AuthSession("token-1", account, role, List.of());

        assertTrue(session.menus().isEmpty());
    }

    @Test
    void testAuthSessionWithMultipleMenus() {
        Account account = new Account("a2", "trader", "pwd", "交易员", UserType.TRADER, "r2", "t1", true);
        Role role = new Role("r2", "TRADER", "交易员", UserType.TRADER, RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW), true);
        List<MenuItem> menus = List.of(
                new MenuItem("t1", null, "交易单管理", "/trade-orders", "TradeOrderPage", "trade", PermissionCode.TRADE_ORDER_VIEW, List.of()),
                new MenuItem("t2", null, "组合交易执行", "/trade-execute", "TradeExecutePage", "execute", PermissionCode.TRADE_ORDER_EXECUTE, List.of())
        );

        AuthSession session = new AuthSession("token-trader-TRADER", account, role, menus);

        assertEquals(2, session.menus().size());
        assertEquals("交易单管理", session.menus().get(0).name());
        assertEquals("组合交易执行", session.menus().get(1).name());
    }

    @Test
    void testAuthSessionEquality() {
        Account account = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Role role = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.values()), true);

        AuthSession session1 = new AuthSession("token-1", account, role, List.of());
        AuthSession session2 = new AuthSession("token-1", account, role, List.of());

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
    }

    @Test
    void testAuthSessionToString() {
        Account account = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Role role = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.values()), true);

        AuthSession session = new AuthSession("token-1", account, role, List.of());
        String str = session.toString();
        assertNotNull(str);
        assertTrue(str.contains("token-1"));
        assertTrue(str.contains("admin"));
    }

    @Test
    void testAuthSessionTokenUniqueness() {
        Account account = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Role role = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.values()), true);

        AuthSession session1 = new AuthSession("token-1", account, role, List.of());
        AuthSession session2 = new AuthSession("token-2", account, role, List.of());

        assertNotEquals(session1.token(), session2.token());
    }

    @Test
    void testAuthSessionWithNullMenus() {
        Account account = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Role role = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.values()), true);

        AuthSession session = new AuthSession("token-1", account, role, null);

        assertNull(session.menus());
    }
}
