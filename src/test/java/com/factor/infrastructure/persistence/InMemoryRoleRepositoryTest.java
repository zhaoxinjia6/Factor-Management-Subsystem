package com.factor.infrastructure.persistence;

import com.factor.domain.auth.PermissionCode;
import com.factor.domain.auth.Role;
import com.factor.domain.auth.RoleScope;
import com.factor.domain.auth.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色仓储内存实现测试
 */
class InMemoryRoleRepositoryTest {

    private InMemoryRoleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRoleRepository();
    }

    @Test
    void testFindByIdExisting() {
        Optional<Role> role = repository.findById("r1");
        assertTrue(role.isPresent());
        assertEquals("SYSTEM_ADMIN", role.get().code());
    }

    @Test
    void testFindByIdTraderRole() {
        Optional<Role> role = repository.findById("r2");
        assertTrue(role.isPresent());
        assertEquals("TRADER", role.get().code());
        assertEquals(UserType.TRADER, role.get().userType());
        assertEquals(RoleScope.BUSINESS, role.get().scope());
    }

    @Test
    void testFindByIdCustomerRole() {
        Optional<Role> role = repository.findById("r3");
        assertTrue(role.isPresent());
        assertEquals("CUSTOMER", role.get().code());
        assertEquals(UserType.CUSTOMER, role.get().userType());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Role> role = repository.findById("non-existent");
        assertFalse(role.isPresent());
    }

    @Test
    void testFindAll() {
        List<Role> roles = repository.findAll();
        assertNotNull(roles);
        assertTrue(roles.size() >= 3);
    }

    @Test
    void testFindAllReturnsImmutable() {
        List<Role> roles = repository.findAll();
        assertThrows(UnsupportedOperationException.class, () -> roles.add(null));
    }

    @Test
    void testSaveNewRole() {
        Role newRole = new Role(null, "NEW_ROLE", "新角色",
                UserType.CUSTOM, RoleScope.CUSTOM, List.of(PermissionCode.EXTENDABLE), false);

        Role saved = repository.save(newRole);
        assertNotNull(saved.id());
        assertEquals("NEW_ROLE", saved.code());
    }

    @Test
    void testSaveExistingRole() {
        Role existing = repository.findById("r1").orElseThrow();
        Role updated = repository.save(new Role(
                "r1", existing.code(), "管理员已更新", existing.userType(),
                existing.scope(), existing.permissions(), existing.builtIn()));

        assertEquals("r1", updated.id());
        assertEquals("管理员已更新", updated.name());

        Optional<Role> found = repository.findById("r1");
        assertTrue(found.isPresent());
        assertEquals("管理员已更新", found.get().name());
    }

    @Test
    void testSaveRoleWithNullId() {
        Role newRole = new Role(null, "TEST_ROLE", "测试角色",
                UserType.CUSTOM, RoleScope.CUSTOM, List.of(), false);

        Role saved = repository.save(newRole);
        assertNotNull(saved.id());
    }

    @Test
    void testDeleteById() {
        // 先创建一个非内置角色
        Role newRole = repository.save(new Role(null, "TO_DELETE", "待删除",
                UserType.CUSTOM, RoleScope.CUSTOM, List.of(), false));

        assertTrue(repository.findById(newRole.id()).isPresent());

        repository.deleteById(newRole.id());
        assertFalse(repository.findById(newRole.id()).isPresent());
    }

    @Test
    void testCannotDeleteBuiltInRole() {
        // InMemoryRoleRepository 的 deleteById 不会删除 builtIn 角色
        repository.deleteById("r1");
        assertTrue(repository.findById("r1").isPresent());
    }

    @Test
    void testSystemAdminPermissions() {
        Optional<Role> role = repository.findById("r1");
        assertTrue(role.isPresent());
        assertTrue(role.get().permissions().size() >= PermissionCode.values().length);
        assertTrue(role.get().builtIn());
    }

    @Test
    void testTraderPermissions() {
        Optional<Role> role = repository.findById("r2");
        assertTrue(role.isPresent());
        assertTrue(role.get().permissions().contains(PermissionCode.TRADE_ORDER_VIEW));
    }

    @Test
    void testCustomerPermissions() {
        Optional<Role> role = repository.findById("r3");
        assertTrue(role.isPresent());
        assertTrue(role.get().permissions().contains(PermissionCode.PORTFOLIO_VIEW));
    }
}
