package com.factor.application.auth;

import com.factor.common.exception.BusinessException;
import com.factor.domain.auth.Account;
import com.factor.domain.auth.AuthSession;
import com.factor.domain.auth.PermissionCode;
import com.factor.domain.auth.Role;
import com.factor.domain.auth.RoleScope;
import com.factor.domain.auth.UserType;
import com.factor.domain.auth.repository.AccountRepository;
import com.factor.domain.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthApplicationServiceImpl authService;

    private Account mockAccount;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockAccount = new Account(
                "1",
                "testuser",
                "password123",
                "测试用户",
                UserType.TRADER,
                "role1",
                "tenant1",
                true
        );
        mockRole = new Role(
                "role1",
                "TRADER_ROLE",
                "交易员角色",
                UserType.TRADER,
                RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW, PermissionCode.PORTFOLIO_VIEW),
                false
        );
    }

    // ==================== login 方法测试 ====================

    @Test
    void login_shouldSucceed_whenCredentialsAreCorrect() {
        when(accountRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockAccount));
        when(roleRepository.findById("role1"))
                .thenReturn(Optional.of(mockRole));

        AuthSession session = authService.login("testuser", "password123", "TRADER");

        assertNotNull(session);
        assertEquals("token-testuser-TRADER", session.token());
        assertEquals(mockAccount, session.account());
        assertEquals(mockRole, session.role());
        assertFalse(session.menus().isEmpty());
    }

    @Test
    void login_shouldThrowException_whenUsernameNotFound() {
        when(accountRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login("unknown", "password123", "TRADER"));

        assertNotNull(exception.getMessage());
    }

    @Test
    void login_shouldThrowException_whenUserTypeMismatch() {
        Account differentTypeAccount = new Account(
                "1",
                "testuser",
                "password123",
                "测试用户",
                UserType.CUSTOMER,
                "role1",
                "tenant1",
                true
        );
        when(accountRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(differentTypeAccount));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login("testuser", "password123", "TRADER"));

        assertNotNull(exception.getMessage());
    }

    @Test
    void login_shouldThrowException_whenAccountDisabled() {
        Account disabledAccount = new Account(
                "1",
                "testuser",
                "password123",
                "测试用户",
                UserType.TRADER,
                "role1",
                "tenant1",
                false
        );
        when(accountRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(disabledAccount));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login("testuser", "password123", "TRADER"));

        assertNotNull(exception.getMessage());
    }

    @Test
    void login_shouldThrowException_whenPasswordWrong() {
        when(accountRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockAccount));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login("testuser", "wrongpassword", "TRADER"));

        assertNotNull(exception.getMessage());
    }

    @Test
    void login_shouldThrowException_whenRoleNotFound() {
        when(accountRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockAccount));
        when(roleRepository.findById("role1"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login("testuser", "password123", "TRADER"));

        assertNotNull(exception.getMessage());
    }

    // ==================== buildMenus 方法间接测试 ====================

    @Test
    void login_shouldReturnAdminMenus_whenUserIsSystemAdmin() {
        Account adminAccount = new Account(
                "2",
                "admin",
                "admin123",
                "管理员",
                UserType.SYSTEM_ADMIN,
                "role2",
                "tenant1",
                true
        );
        Role adminRole = new Role(
                "role2",
                "ADMIN_ROLE",
                "管理员角色",
                UserType.SYSTEM_ADMIN,
                RoleScope.SYSTEM,
                List.of(PermissionCode.values()),
                false
        );

        when(accountRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminAccount));
        when(roleRepository.findById("role2"))
                .thenReturn(Optional.of(adminRole));

        AuthSession session = authService.login("admin", "admin123", "SYSTEM_ADMIN");

        assertNotNull(session);
        assertEquals(10, session.menus().size());
    }

    @Test
    void login_shouldReturnTraderMenus_whenUserIsTrader() {
        when(accountRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockAccount));
        when(roleRepository.findById("role1"))
                .thenReturn(Optional.of(mockRole));

        AuthSession session = authService.login("testuser", "password123", "TRADER");

        assertNotNull(session);
        assertEquals(4, session.menus().size());
    }

    @Test
    void login_shouldReturnCustomerMenus_whenUserIsCustomer() {
        Account customerAccount = new Account(
                "3",
                "customer",
                "customer123",
                "客户",
                UserType.CUSTOMER,
                "role3",
                "tenant1",
                true
        );
        Role customerRole = new Role(
                "role3",
                "CUSTOMER_ROLE",
                "客户角色",
                UserType.CUSTOMER,
                RoleScope.CUSTOM,
                List.of(PermissionCode.PORTFOLIO_VIEW, PermissionCode.AGREEMENT_VIEW),
                false
        );

        when(accountRepository.findByUsername("customer"))
                .thenReturn(Optional.of(customerAccount));
        when(roleRepository.findById("role3"))
                .thenReturn(Optional.of(customerRole));

        AuthSession session = authService.login("customer", "customer123", "CUSTOMER");

        assertNotNull(session);
        assertEquals(8, session.menus().size());
    }
}