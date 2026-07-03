package com.factor.domain.auth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色领域模型测试
 */
class RoleTest {

    @Test
    void testCreateRole() {
        Role role = new Role("r1", "SYSTEM_ADMIN", "系统超级管理员",
                UserType.SYSTEM_ADMIN, RoleScope.SYSTEM, List.of(PermissionCode.values()), true);

        assertEquals("r1", role.id());
        assertEquals("SYSTEM_ADMIN", role.code());
        assertEquals("系统超级管理员", role.name());
        assertEquals(UserType.SYSTEM_ADMIN, role.userType());
        assertEquals(RoleScope.SYSTEM, role.scope());
        assertTrue(role.builtIn());
        assertTrue(role.permissions().contains(PermissionCode.TENANT_MANAGE));
    }

    @Test
    void testCreateRoleWithCustomPermission() {
        Role role = new Role("r2", "TRADER", "交易员",
                UserType.TRADER, RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW, PermissionCode.TRADE_ORDER_EXECUTE), true);

        assertEquals(2, role.permissions().size());
        assertTrue(role.permissions().contains(PermissionCode.TRADE_ORDER_VIEW));
        assertTrue(role.permissions().contains(PermissionCode.TRADE_ORDER_EXECUTE));
        assertFalse(role.permissions().contains(PermissionCode.TENANT_MANAGE));
    }

    @Test
    void testNonBuiltInRole() {
        Role role = new Role("r4", "CUSTOM_ROLE", "自定义角色",
                UserType.CUSTOM, RoleScope.CUSTOM, List.of(PermissionCode.EXTENDABLE), false);

        assertFalse(role.builtIn());
        assertEquals(RoleScope.CUSTOM, role.scope());
    }

    @Test
    void testRoleWithEmptyPermissions() {
        Role role = new Role("r5", "EMPTY", "空权限角色",
                UserType.CUSTOM, RoleScope.CUSTOM, List.of(), false);

        assertTrue(role.permissions().isEmpty());
    }

    @Test
    void testRoleEquality() {
        Role role1 = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.TENANT_MANAGE), true);
        Role role2 = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.TENANT_MANAGE), true);

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    void testRoleToString() {
        Role role = new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.TENANT_MANAGE), true);

        String str = role.toString();
        assertNotNull(str);
        assertTrue(str.contains("r1"));
        assertTrue(str.contains("SYSTEM_ADMIN"));
    }

    @Test
    void testDifferentRoleScopes() {
        Role sysRole = new Role("r1", "SYS", "系统", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM, List.of(), true);
        Role bizRole = new Role("r2", "BIZ", "业务", UserType.TRADER, RoleScope.BUSINESS, List.of(), true);
        Role customRole = new Role("r3", "CUSTOM", "自定义", UserType.CUSTOM, RoleScope.CUSTOM, List.of(), false);

        assertEquals(RoleScope.SYSTEM, sysRole.scope());
        assertEquals(RoleScope.BUSINESS, bizRole.scope());
        assertEquals(RoleScope.CUSTOM, customRole.scope());
    }
}
