package com.factor.infrastructure.persistence.jpa;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaRoleRepositoryTest {

    @Mock
    private RoleJpaRepository roleJpaRepository;

    @InjectMocks
    private JpaRoleRepository jpaRoleRepository;

    private RoleEntity mockEntity;
    private Role mockDomain;

    @BeforeEach
    void setUp() {
        mockEntity = new RoleEntity();
        mockEntity.setId("role1");
        mockEntity.setCode("TEST_ROLE");
        mockEntity.setName("测试角色");
        mockEntity.setUserType("TRADER");
        mockEntity.setScope("BUSINESS");
        mockEntity.setPermissions("TRADE_ORDER_VIEW,PORTFOLIO_VIEW");
        mockEntity.setBuiltIn(false);

        mockDomain = new Role("role1", "TEST_ROLE", "测试角色", UserType.TRADER,
                RoleScope.BUSINESS, List.of(PermissionCode.TRADE_ORDER_VIEW, PermissionCode.PORTFOLIO_VIEW), false);
    }

    // ==================== findById 测试 ====================

    @Test
    void findById_shouldReturnRole_whenExists() {
        when(roleJpaRepository.findById("role1")).thenReturn(Optional.of(mockEntity));

        Optional<Role> result = jpaRoleRepository.findById("role1");

        assertTrue(result.isPresent());
        assertEquals("TEST_ROLE", result.get().code());
        assertEquals(2, result.get().permissions().size());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(roleJpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Role> result = jpaRoleRepository.findById("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ==================== findAll 测试 ====================

    @Test
    void findAll_shouldReturnAllRoles() {
        when(roleJpaRepository.findAll()).thenReturn(List.of(mockEntity));

        List<Role> result = jpaRoleRepository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST_ROLE", result.get(0).code());
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoRoles() {
        when(roleJpaRepository.findAll()).thenReturn(List.of());

        List<Role> result = jpaRoleRepository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== save 测试 ====================

    @Test
    void save_shouldCreateNew_whenIdIsNull() {
        Role newRole = new Role(null, "NEW_ROLE", "新角色", UserType.CUSTOMER,
                RoleScope.CUSTOM, List.of(PermissionCode.PORTFOLIO_VIEW), false);

        when(roleJpaRepository.save(any(RoleEntity.class)))
                .thenReturn(mockEntity);

        Role result = jpaRoleRepository.save(newRole);

        assertNotNull(result);
        verify(roleJpaRepository, times(1)).save(any(RoleEntity.class));
    }

    @Test
    void save_shouldUpdateExisting_whenIdIsNotNull() {
        when(roleJpaRepository.save(any(RoleEntity.class)))
                .thenReturn(mockEntity);

        Role result = jpaRoleRepository.save(mockDomain);

        assertNotNull(result);
        verify(roleJpaRepository, times(1)).save(any(RoleEntity.class));
    }

    // ==================== deleteById 测试 ====================

    @Test
    void deleteById_shouldDelete_whenRoleNotBuiltIn() {
        when(roleJpaRepository.findById("role1")).thenReturn(Optional.of(mockEntity));
        doNothing().when(roleJpaRepository).deleteById("role1");

        jpaRoleRepository.deleteById("role1");

        verify(roleJpaRepository, times(1)).deleteById("role1");
    }

    @Test
    void deleteById_shouldNotDelete_whenRoleIsBuiltIn() {
        mockEntity.setBuiltIn(true);
        when(roleJpaRepository.findById("role1")).thenReturn(Optional.of(mockEntity));

        jpaRoleRepository.deleteById("role1");

        verify(roleJpaRepository, never()).deleteById(anyString());
    }

    @Test
    void deleteById_shouldDoNothing_whenRoleNotFound() {
        when(roleJpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        jpaRoleRepository.deleteById("nonexistent");

        verify(roleJpaRepository, never()).deleteById(anyString());
    }
}