package com.factor.application.auth;

import com.factor.common.exception.BusinessException;
import com.factor.domain.auth.*;
import com.factor.domain.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色应用服务测试
 */
class RoleApplicationServiceTest {

    private RoleApplicationService roleService;
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository = new InMemoryRoleRepositoryStub();
        roleService = new RoleApplicationServiceImpl(roleRepository);
    }

    @Test
    void testListRoles() {
        List<Role> roles = roleService.listRoles();
        assertNotNull(roles);
        assertTrue(roles.size() >= 3);
    }

    @Test
    void testGetRoleExisting() {
        Optional<Role> role = roleService.getRole("r1");
        assertTrue(role.isPresent());
        assertEquals("SYSTEM_ADMIN", role.get().code());
    }

    @Test
    void testGetRoleNotFound() {
        Optional<Role> role = roleService.getRole("non-existent");
        assertFalse(role.isPresent());
    }

    @Test
    void testCreateRole() {
        Role newRole = new Role(null, "CUSTOM_ROLE", "自定义角色",
                UserType.CUSTOM, RoleScope.CUSTOM,
                List.of(PermissionCode.EXTENDABLE), false);

        Role created = roleService.createRole(newRole);
        assertNotNull(created.id());
        assertEquals("CUSTOM_ROLE", created.code());
        assertEquals("自定义角色", created.name());
    }

    @Test
    void testUpdateExistingRole() {
        Role updated = roleService.updateRole("r1", new Role("r1", "ADMIN_UPDATED", "管理员更新",
                UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                List.of(PermissionCode.TENANT_MANAGE, PermissionCode.ACCOUNT_MANAGE), true));

        assertNotNull(updated);
        assertEquals("ADMIN_UPDATED", updated.code());
        assertEquals("管理员更新", updated.name());
    }

    @Test
    void testUpdateNonExistentRole() {
        assertThrows(BusinessException.class, () ->
                roleService.updateRole("non-existent", new Role(null, "TEST", "测试",
                        UserType.CUSTOM, RoleScope.CUSTOM, List.of(), false))
        );
    }

    @Test
    void testDeleteNonBuiltInRole() {
        // 先创建一个非内置角色
        Role customRole = roleService.createRole(new Role(null, "DELETE_TEST", "待删除",
                UserType.CUSTOM, RoleScope.CUSTOM, List.of(PermissionCode.EXTENDABLE), false));

        assertDoesNotThrow(() -> roleService.deleteRole(customRole.id()));
    }

    @Test
    void testDeleteBuiltInRole() {
        assertThrows(BusinessException.class, () ->
                roleService.deleteRole("r1")
        );
    }

    @Test
    void testListTemplatePermissionsForAdmin() {
        List<PermissionCode> perms = roleService.listTemplatePermissions("SYSTEM_ADMIN");
        assertNotNull(perms);
        assertTrue(perms.contains(PermissionCode.TENANT_MANAGE));
    }

    @Test
    void testListTemplatePermissionsForTrader() {
        List<PermissionCode> perms = roleService.listTemplatePermissions("TRADER");
        assertNotNull(perms);
        assertTrue(perms.contains(PermissionCode.TRADE_ORDER_VIEW));
        assertFalse(perms.contains(PermissionCode.TENANT_MANAGE));
    }

    @Test
    void testListTemplatePermissionsForCustomer() {
        List<PermissionCode> perms = roleService.listTemplatePermissions("CUSTOMER");
        assertNotNull(perms);
        assertTrue(perms.contains(PermissionCode.PORTFOLIO_VIEW));
    }

    /**
     * 测试用 RoleRepository Stub
     */
    private static class InMemoryRoleRepositoryStub implements RoleRepository {
        private final List<Role> storage = new ArrayList<>(List.of(
                new Role("r1", "SYSTEM_ADMIN", "系统超级管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                        List.of(PermissionCode.values()), true),
                new Role("r2", "TRADER", "交易员", UserType.TRADER, RoleScope.BUSINESS,
                        List.of(PermissionCode.TRADE_ORDER_VIEW, PermissionCode.TRADE_ORDER_EXECUTE), true),
                new Role("r3", "CUSTOMER", "客户", UserType.CUSTOMER, RoleScope.BUSINESS,
                        List.of(PermissionCode.PORTFOLIO_VIEW), true)
        ));

        @Override
        public Optional<Role> findById(String id) {
            return storage.stream().filter(r -> r.id().equals(id)).findFirst();
        }

        @Override
        public List<Role> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public Role save(Role role) {
            Role stored = role.id() == null
                    ? new Role(UUID.randomUUID().toString(), role.code(), role.name(), role.userType(), role.scope(), role.permissions(), role.builtIn())
                    : role;
            storage.removeIf(existing -> existing.id().equals(stored.id()));
            storage.add(stored);
            return stored;
        }

        @Override
        public void deleteById(String id) {
            storage.removeIf(role -> role.id().equals(id) && !role.builtIn());
        }
    }
}
