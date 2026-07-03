package com.factor.domain.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账号领域模型测试
 */
class AccountTest {

    @Test
    void testCreateAccount() {
        Account account = new Account("a1", "admin", "admin123", "系统超级管理员",
                UserType.SYSTEM_ADMIN, "r1", null, true);

        assertEquals("a1", account.id());
        assertEquals("admin", account.username());
        assertEquals("admin123", account.passwordHash());
        assertEquals("系统超级管理员", account.displayName());
        assertEquals(UserType.SYSTEM_ADMIN, account.userType());
        assertEquals("r1", account.roleId());
        assertNull(account.tenantId());
        assertTrue(account.enabled());
    }

    @Test
    void testCreateDisabledAccount() {
        Account account = new Account("a2", "disabled", "pwd", "禁用用户",
                UserType.TRADER, "r2", "t1", false);

        assertFalse(account.enabled());
        assertEquals("t1", account.tenantId());
    }

    @Test
    void testAccountWithTenantId() {
        Account account = new Account("a3", "customer", "customer123", "客户",
                UserType.CUSTOMER, "r3", "t1", true);

        assertEquals("t1", account.tenantId());
    }

    @Test
    void testAccountUserTypes() {
        Account admin = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Account trader = new Account("a2", "trader", "pwd", "交易员", UserType.TRADER, "r2", null, true);
        Account customer = new Account("a3", "customer", "pwd", "客户", UserType.CUSTOMER, "r3", null, true);

        assertEquals(UserType.SYSTEM_ADMIN, admin.userType());
        assertEquals(UserType.TRADER, trader.userType());
        assertEquals(UserType.CUSTOMER, customer.userType());
    }

    @Test
    void testAccountEquality() {
        Account account1 = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);
        Account account2 = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);

        assertEquals(account1, account2);
        assertEquals(account1.hashCode(), account2.hashCode());
    }

    @Test
    void testAccountToString() {
        Account account = new Account("a1", "admin", "pwd", "管理员", UserType.SYSTEM_ADMIN, "r1", null, true);

        String str = account.toString();
        assertNotNull(str);
        assertTrue(str.contains("admin"));
        assertTrue(str.contains("SYSTEM_ADMIN"));
    }

    @Test
    void testAccountWithNullRoleId() {
        Account account = new Account("a4", "nobody", "pwd", "无角色", UserType.CUSTOM, null, null, true);

        assertNull(account.roleId());
        assertNull(account.tenantId());
    }

    @Test
    void testAccountWithDifferentTenantIds() {
        Account accountT1 = new Account("a1", "user1", "pwd", "用户1", UserType.TRADER, "r1", "t1", true);
        Account accountT2 = new Account("a2", "user2", "pwd", "用户2", UserType.TRADER, "r2", "t2", true);

        assertEquals("t1", accountT1.tenantId());
        assertEquals("t2", accountT2.tenantId());
    }
}
