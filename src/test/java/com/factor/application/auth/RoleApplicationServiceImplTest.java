package com.factor.application.auth;

import com.factor.common.exception.BusinessException;
import com.factor.domain.auth.PermissionCode;
import com.factor.domain.auth.Role;
import com.factor.domain.auth.RoleScope;
import com.factor.domain.auth.UserType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleApplicationServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleApplicationServiceImpl roleService;

    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role(
                "role1",
                "TEST_ROLE",
                "测试角色",
                UserType.TRADER,
                RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW),
                false
        );
    }

    // ==================== listRoles 方法测试 ====================

    @Test
    void listRoles_shouldReturnAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(mockRole));

        List<Role> roles = roleService.listRoles();

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("TEST_ROLE", roles.get(0).code());
    }

    @Test
    void listRoles_shouldReturnEmptyList_whenNoRoles() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<Role> roles = roleService.listRoles();

        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    // ==================== getRole 方法测试 ====================

    @Test
    void getRole_shouldReturnRole_whenExists() {
        when(roleRepository.findById("role1")).thenReturn(Optional.of(mockRole));

        Optional<Role> result = roleService.getRole("role1");

        assertTrue(result.isPresent());
        assertEquals("TEST_ROLE", result.get().code());
    }

    @Test
    void getRole_shouldReturnEmpty_whenNotExists() {
        when(roleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Role> result = roleService.getRole("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ==================== createRole 方法测试 ====================

    @Test
    void createRole_shouldSaveAndReturnRole() {
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        Role result = roleService.createRole(mockRole);

        assertNotNull(result);
        verify(roleRepository, times(1)).save(mockRole);
    }

    // ==================== updateRole 方法测试 ====================

    @Test
    void updateRole_shouldUpdate_whenRoleExistsAndNotBuiltIn() {
        Role existingRole = new Role(
                "role1",
                "OLD_CODE",
                "旧名称",
                UserType.TRADER,
                RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW),
                false
        );
        Role updatedRole = new Role(
                "role1",
                "NEW_CODE",
                "新名称",
                UserType.TRADER,
                RoleScope.BUSINESS,
                List.of(PermissionCode.PORTFOLIO_VIEW),
                false
        );

        when(roleRepository.findById("role1")).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        Role result = roleService.updateRole("role1", updatedRole);

        assertNotNull(result);
        assertEquals("NEW_CODE", result.code());
        assertEquals("新名称", result.name());
    }

    @Test
    void updateRole_shouldKeepBuiltInTrue_whenExistingIsBuiltIn() {
        Role builtInRole = new Role(
                "role1",
                "BUILT_IN",
                "内置角色",
                UserType.TRADER,
                RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW),
                true
        );
        Role updatedRole = new Role(
                "role1",
                "NEW_CODE",
                "新名称",
                UserType.TRADER,
                RoleScope.BUSINESS,
                List.of(PermissionCode.PORTFOLIO_VIEW),
                false
        );

        when(roleRepository.findById("role1")).thenReturn(Optional.of(builtInRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role result = roleService.updateRole("role1", updatedRole);

        assertNotNull(result);
        assertTrue(result.builtIn());
    }

    @Test
    void updateRole_shouldThrowException_whenRoleNotFound() {
        when(roleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> roleService.updateRole("nonexistent", mockRole));

        assertNotNull(exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    // ==================== deleteRole 方法测试 ====================

    @Test
    void deleteRole_shouldDelete_whenRoleExistsAndNotBuiltIn() {
        when(roleRepository.findById("role1")).thenReturn(Optional.of(mockRole));
        doNothing().when(roleRepository).deleteById("role1");

        roleService.deleteRole("role1");

        verify(roleRepository, times(1)).deleteById("role1");
    }

    @Test
    void deleteRole_shouldThrowException_whenRoleIsBuiltIn() {
        Role builtInRole = new Role(
                "role1",
                "BUILT_IN",
                "内置角色",
                UserType.TRADER,
                RoleScope.BUSINESS,
                List.of(PermissionCode.TRADE_ORDER_VIEW),
                true
        );
        when(roleRepository.findById("role1")).thenReturn(Optional.of(builtInRole));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> roleService.deleteRole("role1"));

        assertNotNull(exception.getMessage());
        verify(roleRepository, never()).deleteById(anyString());
    }

    @Test
    void deleteRole_shouldThrowException_whenRoleNotFound() {
        when(roleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> roleService.deleteRole("nonexistent"));

        assertNotNull(exception.getMessage());
        verify(roleRepository, never()).deleteById(anyString());
    }

    // ==================== listTemplatePermissions 方法测试 ====================

    @Test
    void listTemplatePermissions_shouldReturnPermissions_forSystemAdmin() {
        List<PermissionCode> permissions = roleService.listTemplatePermissions("SYSTEM_ADMIN");

        assertNotNull(permissions);
        assertEquals(PermissionCode.values().length, permissions.size());
    }

    @Test
    void listTemplatePermissions_shouldReturnPermissions_forTrader() {
        List<PermissionCode> permissions = roleService.listTemplatePermissions("TRADER");

        assertNotNull(permissions);
        assertEquals(5, permissions.size());
    }

    @Test
    void listTemplatePermissions_shouldReturnPermissions_forCustomer() {
        List<PermissionCode> permissions = roleService.listTemplatePermissions("CUSTOMER");

        assertNotNull(permissions);
        assertEquals(5, permissions.size());
    }
}