package com.factor.application.auth;

import com.factor.domain.auth.PermissionCode;
import com.factor.domain.auth.UserType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleTemplateRegistryTest {

    @Test
    void defaultPermissions_shouldReturnAllPermissions_forSystemAdmin() {
        Set<PermissionCode> permissions = RoleTemplateRegistry.defaultPermissions(UserType.SYSTEM_ADMIN);

        assertNotNull(permissions);
        assertEquals(PermissionCode.values().length, permissions.size());
    }

    @Test
    void defaultPermissions_shouldReturnTraderPermissions_forTrader() {
        Set<PermissionCode> permissions = RoleTemplateRegistry.defaultPermissions(UserType.TRADER);

        assertNotNull(permissions);
        assertTrue(permissions.contains(PermissionCode.TRADE_ORDER_VIEW));
        assertTrue(permissions.contains(PermissionCode.TRADE_ORDER_EXECUTE));
        assertTrue(permissions.contains(PermissionCode.PORTFOLIO_VIEW));
        assertFalse(permissions.contains(PermissionCode.TENANT_MANAGE));
    }

    @Test
    void defaultPermissions_shouldReturnCustomerPermissions_forCustomer() {
        Set<PermissionCode> permissions = RoleTemplateRegistry.defaultPermissions(UserType.CUSTOMER);

        assertNotNull(permissions);
        assertTrue(permissions.contains(PermissionCode.PORTFOLIO_VIEW));
        assertTrue(permissions.contains(PermissionCode.AGREEMENT_VIEW));
        assertTrue(permissions.contains(PermissionCode.AGREEMENT_SIGN));
        assertFalse(permissions.contains(PermissionCode.TRADE_ORDER_VIEW));
    }

    @Test
    void defaultPermissions_shouldReturnExtendableOnly_forUnknownUserType() {
        // 使用一个不存在的 UserType 值测试默认行为
        Set<PermissionCode> permissions = RoleTemplateRegistry.defaultPermissions(null);

        assertNotNull(permissions);
        assertEquals(1, permissions.size());
        assertTrue(permissions.contains(PermissionCode.EXTENDABLE));
    }
}