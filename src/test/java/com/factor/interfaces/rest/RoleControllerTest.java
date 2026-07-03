package com.factor.interfaces.rest;

import com.factor.application.auth.RoleApplicationService;
import com.factor.common.api.ApiResponse;
import com.factor.domain.auth.*;
import com.factor.interfaces.rest.dto.RoleUpsertRequest;
import com.factor.interfaces.rest.vo.RoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色控制器测试
 */
class RoleControllerTest {

    private RoleController controller;
    private RoleApplicationServiceStub roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleApplicationServiceStub();
        controller = new RoleController(roleService);
    }

    @Test
    void testListRoles() {
        ApiResponse<List<RoleVO>> response = controller.list();
        assertTrue(response.success());
        assertNotNull(response.data());
        assertTrue(response.data().size() > 0);
    }

    @Test
    void testListRolesContainsAdmin() {
        ApiResponse<List<RoleVO>> response = controller.list();
        assertTrue(response.data().stream().anyMatch(r -> r.code().equals("SYSTEM_ADMIN")));
    }

    @Test
    void testListRolesContainsTrader() {
        ApiResponse<List<RoleVO>> response = controller.list();
        assertTrue(response.data().stream().anyMatch(r -> r.code().equals("TRADER")));
    }

    @Test
    void testCreateRole() {
        RoleUpsertRequest request = new RoleUpsertRequest(
                "NEW_ROLE", "新角色", "CUSTOM", "CUSTOM",
                List.of("EXTENDABLE"), false
        );

        ApiResponse<RoleVO> response = controller.create(request);
        assertTrue(response.success());
        assertNotNull(response.data());
        assertEquals("NEW_ROLE", response.data().code());
    }

    @Test
    void testUpdateRole() {
        RoleUpsertRequest request = new RoleUpsertRequest(
                "ADMIN_UPDATED", "管理员更新", "SYSTEM_ADMIN", "SYSTEM",
                List.of("TENANT_MANAGE", "ACCOUNT_MANAGE"), true
        );

        ApiResponse<RoleVO> response = controller.update("r1", request);
        assertTrue(response.success());
        assertEquals("ADMIN_UPDATED", response.data().code());
    }

    @Test
    void testDeleteRole() {
        ApiResponse<Void> response = controller.delete("r4");
        assertTrue(response.success());
    }

    @Test
    void testListTemplates() {
        ApiResponse<List<PermissionCode>> response = controller.template("SYSTEM_ADMIN");
        assertTrue(response.success());
        assertNotNull(response.data());
        assertTrue(response.data().contains(PermissionCode.TENANT_MANAGE));
    }

    /**
     * RoleApplicationService 测试桩
     */
    private static class RoleApplicationServiceStub implements RoleApplicationService {

        @Override
        public List<Role> listRoles() {
            return List.of(
                    new Role("r1", "SYSTEM_ADMIN", "系统超级管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM,
                            List.of(PermissionCode.values()), true),
                    new Role("r2", "TRADER", "交易员", UserType.TRADER, RoleScope.BUSINESS,
                            List.of(PermissionCode.TRADE_ORDER_VIEW), true)
            );
        }

        @Override
        public Optional<Role> getRole(String id) {
            return id.equals("r1") ? Optional.of(new Role("r1", "SYSTEM_ADMIN", "管理员", UserType.SYSTEM_ADMIN, RoleScope.SYSTEM, List.of(), true)) : Optional.empty();
        }

        @Override
        public Role createRole(Role role) {
            return new Role("new-id", role.code(), role.name(), role.userType(), role.scope(), role.permissions(), role.builtIn());
        }

        @Override
        public Role updateRole(String id, Role role) {
            return new Role(id, role.code(), role.name(), role.userType(), role.scope(), role.permissions(), role.builtIn());
        }

        @Override
        public void deleteRole(String id) {
        }

        @Override
        public List<PermissionCode> listTemplatePermissions(String userType) {
            return List.of(PermissionCode.TENANT_MANAGE, PermissionCode.ACCOUNT_MANAGE);
        }
    }
}
