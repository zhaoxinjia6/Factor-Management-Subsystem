package com.factor.application.auth;

import com.factor.common.exception.BusinessException;
import com.factor.domain.auth.*;
import com.factor.domain.auth.repository.AccountRepository;
import com.factor.domain.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证应用服务测试
 */
class AuthApplicationServiceTest {

    private AuthApplicationService authService;
    private AccountRepository accountRepository;
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepositoryStub();
        roleRepository = new InMemoryRoleRepositoryStub();
        authService = new AuthApplicationServiceImpl(accountRepository, roleRepository);
    }

    @Test
    void testLoginAdminSuccess() {
        AuthSession session = authService.login("admin", "admin123", "SYSTEM_ADMIN");

        assertNotNull(session);
        assertNotNull(session.token());
        assertEquals("admin", session.account().username());
        assertEquals(UserType.SYSTEM_ADMIN, session.account().userType());
        assertNotNull(session.role());
        assertNotNull(session.menus());
        assertTrue(session.menus().size() > 0);
    }

    @Test
    void testLoginTraderSuccess() {
        AuthSession session = authService.login("trader", "trader123", "TRADER");

        assertNotNull(session);
        assertEquals("trader", session.account().username());
        assertEquals(UserType.TRADER, session.account().userType());
    }

    @Test
    void testLoginCustomerSuccess() {
        AuthSession session = authService.login("customer", "customer123", "CUSTOMER");

        assertNotNull(session);
        assertEquals("customer", session.account().username());
        assertEquals(UserType.CUSTOMER, session.account().userType());
    }

    @Test
    void testLoginWithWrongPassword() {
        assertThrows(BusinessException.class, () ->
                authService.login("admin", "wrong-password", "SYSTEM_ADMIN")
        );
    }

    @Test
    void testLoginWithWrongUsername() {
        assertThrows(BusinessException.class, () ->
                authService.login("non-existent", "pwd", "SYSTEM_ADMIN")
        );
    }

    @Test
    void testLoginWithWrongUserType() {
        assertThrows(BusinessException.class, () ->
                authService.login("admin", "admin123", "TRADER")
        );
    }

    @Test
    void testLoginAdminHasSystemMenus() {
        AuthSession session = authService.login("admin", "admin123", "SYSTEM_ADMIN");

        List<MenuItem> menus = session.menus();
        assertTrue(menus.stream().anyMatch(m -> m.name().equals("租户管理")));
        assertTrue(menus.stream().anyMatch(m -> m.name().equals("账号管理")));
        assertTrue(menus.stream().anyMatch(m -> m.name().equals("角色模板配置")));
    }

    @Test
    void testLoginTraderHasTradeMenus() {
        AuthSession session = authService.login("trader", "trader123", "TRADER");

        List<MenuItem> menus = session.menus();
        assertTrue(menus.stream().anyMatch(m -> m.name().equals("交易单管理")));
        assertTrue(menus.stream().anyMatch(m -> m.name().equals("组合交易执行")));
    }

    @Test
    void testLoginCustomerHasCustomerMenus() {
        AuthSession session = authService.login("customer", "customer123", "CUSTOMER");

        List<MenuItem> menus = session.menus();
        assertTrue(menus.stream().anyMatch(m -> m.name().equals("我的产品")));
        assertTrue(menus.stream().anyMatch(m -> m.name().equals("我的组合")));
    }

    @Test
    void testLoginTokenFormat() {
        AuthSession session = authService.login("admin", "admin123", "SYSTEM_ADMIN");

        assertTrue(session.token().startsWith("token-"));
        assertTrue(session.token().contains("admin"));
        assertTrue(session.token().contains("SYSTEM_ADMIN"));
    }

    /**
     * 测试用内存实现 - AccountRepository
     */
    private static class InMemoryAccountRepositoryStub implements AccountRepository {
        private final List<Account> accounts = List.of(
                new Account("a1", "admin", "admin123", "系统超级管理员", UserType.SYSTEM_ADMIN, "r1", null, true),
                new Account("a2", "trader", "trader123", "交易员", UserType.TRADER, "r2", "t1", true),
                new Account("a3", "customer", "customer123", "客户", UserType.CUSTOMER, "r3", "t1", true)
        );

        @Override
        public Optional<Account> findByUsername(String username) {
            return accounts.stream().filter(a -> a.username().equals(username)).findFirst();
        }
    }

    /**
     * 测试用内存实现 - RoleRepository
     */
    private static class InMemoryRoleRepositoryStub implements RoleRepository {
        private final List<Role> roles = List.of(
                new Role("r1", "SYSTEM_ADMIN", "系统超级管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                        List.of(PermissionCode.values()), true),
                new Role("r2", "TRADER", "交易员", UserType.TRADER, RoleScope.BUSINESS,
                        List.of(PermissionCode.TRADE_ORDER_VIEW, PermissionCode.TRADE_ORDER_EXECUTE,
                                PermissionCode.PORTFOLIO_VIEW), true),
                new Role("r3", "CUSTOMER", "客户", UserType.CUSTOMER, RoleScope.BUSINESS,
                        List.of(PermissionCode.PORTFOLIO_VIEW, PermissionCode.AGREEMENT_VIEW), true)
        );

        @Override
        public Optional<Role> findById(String id) {
            return roles.stream().filter(r -> r.id().equals(id)).findFirst();
        }

        @Override
        public List<Role> findAll() {
            return List.copyOf(roles);
        }

        @Override
        public Role save(Role role) {
            return role;
        }

        @Override
        public void deleteById(String id) {
        }
    }
}
