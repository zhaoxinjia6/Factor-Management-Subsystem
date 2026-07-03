package com.factor.infrastructure.persistence;

import com.factor.domain.auth.Account;
import com.factor.domain.auth.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账号仓储内存实现测试
 */
class InMemoryAccountRepositoryTest {

    private InMemoryAccountRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();
    }

    @Test
    void testFindByUsernameExisting() {
        Optional<Account> account = repository.findByUsername("admin");
        assertTrue(account.isPresent());
        assertEquals("admin", account.get().username());
        assertEquals(UserType.SYSTEM_ADMIN, account.get().userType());
    }

    @Test
    void testFindByUsernameTrader() {
        Optional<Account> account = repository.findByUsername("trader");
        assertTrue(account.isPresent());
        assertEquals("trader", account.get().username());
        assertEquals(UserType.TRADER, account.get().userType());
        assertEquals("t1", account.get().tenantId());
    }

    @Test
    void testFindByUsernameCustomer() {
        Optional<Account> account = repository.findByUsername("customer");
        assertTrue(account.isPresent());
        assertEquals("customer", account.get().username());
        assertEquals(UserType.CUSTOMER, account.get().userType());
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<Account> account = repository.findByUsername("non-existent-user");
        assertFalse(account.isPresent());
    }

    @Test
    void testFindByUsernameEmptyString() {
        Optional<Account> account = repository.findByUsername("");
        assertFalse(account.isPresent());
    }

    @Test
    void testFindByUsernameNull() {
        Optional<Account> account = repository.findByUsername(null);
        assertFalse(account.isPresent());
    }

    @Test
    void testAdminAccountDetails() {
        Optional<Account> account = repository.findByUsername("admin");
        assertTrue(account.isPresent());

        Account admin = account.get();
        assertEquals("a1", admin.id());
        assertEquals("admin123", admin.passwordHash());
        assertEquals("系统超级管理员", admin.displayName());
        assertEquals("r1", admin.roleId());
        assertNull(admin.tenantId());
        assertTrue(admin.enabled());
    }

    @Test
    void testTraderAccountDetails() {
        Optional<Account> account = repository.findByUsername("trader");
        assertTrue(account.isPresent());

        Account trader = account.get();
        assertEquals("a2", trader.id());
        assertEquals("trader123", trader.passwordHash());
        assertEquals("交易员", trader.displayName());
        assertEquals("r2", trader.roleId());
        assertEquals("t1", trader.tenantId());
        assertTrue(trader.enabled());
    }

    @Test
    void testCustomerAccountDetails() {
        Optional<Account> account = repository.findByUsername("customer");
        assertTrue(account.isPresent());

        Account customer = account.get();
        assertEquals("a3", customer.id());
        assertEquals("customer123", customer.passwordHash());
        assertEquals("客户", customer.displayName());
        assertEquals("r3", customer.roleId());
        assertEquals("t1", customer.tenantId());
        assertTrue(customer.enabled());
    }

    @Test
    void testAccountCaseSensitivity() {
        Optional<Account> account = repository.findByUsername("Admin");
        assertFalse(account.isPresent());
    }
}
