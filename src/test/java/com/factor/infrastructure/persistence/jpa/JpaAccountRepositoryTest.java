package com.factor.infrastructure.persistence.jpa;

import com.factor.domain.auth.Account;
import com.factor.domain.auth.UserType;
import com.factor.domain.auth.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaAccountRepositoryTest {

    @Mock
    private AccountJpaRepository accountJpaRepository;

    @InjectMocks
    private JpaAccountRepository jpaAccountRepository;

    private AccountEntity mockEntity;

    @BeforeEach
    void setUp() {
        mockEntity = new AccountEntity();
        mockEntity.setId("a1");
        mockEntity.setUsername("testuser");
        mockEntity.setPasswordHash("password123");
        mockEntity.setDisplayName("测试用户");
        mockEntity.setUserType("TRADER");
        mockEntity.setRoleId("role1");
        mockEntity.setTenantId("tenant1");
        mockEntity.setEnabled(true);
    }

    // ==================== findByUsername 测试 ====================

    @Test
    void findByUsername_shouldReturnAccount_whenExists() {
        when(accountJpaRepository.findByUsername("testuser")).thenReturn(Optional.of(mockEntity));

        Optional<Account> result = jpaAccountRepository.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().username());
        assertEquals("测试用户", result.get().displayName());
        assertEquals(UserType.TRADER, result.get().userType());
        assertTrue(result.get().enabled());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        when(accountJpaRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<Account> result = jpaAccountRepository.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_shouldMapAllFieldsCorrectly() {
        when(accountJpaRepository.findByUsername("testuser")).thenReturn(Optional.of(mockEntity));

        Optional<Account> result = jpaAccountRepository.findByUsername("testuser");

        assertTrue(result.isPresent());
        Account account = result.get();
        assertEquals("a1", account.id());
        assertEquals("password123", account.passwordHash());
        assertEquals("role1", account.roleId());
        assertEquals("tenant1", account.tenantId());
    }

    @Test
    void findByUsername_shouldHandleDisabledAccount() {
        mockEntity.setEnabled(false);
        when(accountJpaRepository.findByUsername("disabled")).thenReturn(Optional.of(mockEntity));

        Optional<Account> result = jpaAccountRepository.findByUsername("disabled");

        assertTrue(result.isPresent());
        assertFalse(result.get().enabled());
    }
}
