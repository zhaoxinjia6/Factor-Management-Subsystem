package com.factor.infrastructure.persistence.jpa;

import com.factor.domain.factor.DerivativeFactor;
import com.factor.domain.factor.repository.DerivativeFactorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaDerivativeFactorRepositoryTest {

    @Mock
    private DerivativeFactorJpaRepository derivativeFactorJpaRepository;

    @InjectMocks
    private JpaDerivativeFactorRepository jpaDerivativeFactorRepository;

    private DerivativeFactorEntity mockEntity;
    private DerivativeFactor mockDomain;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        mockEntity = new DerivativeFactorEntity();
        mockEntity.setId("d1");
        mockEntity.setCode("derived_roe");
        mockEntity.setName("衍生ROE");
        mockEntity.setCreatedBy("admin");
        mockEntity.setCreatedAt(now);
        mockEntity.setDescription("公式: ROE * 1.2");
        mockEntity.setFormula("ROE * 1.2");
        mockEntity.setEnabled(true);

        mockDomain = new DerivativeFactor("d1", "derived_roe", "衍生ROE", "admin",
                now, "公式: ROE * 1.2", "ROE * 1.2", true);
    }

    // ==================== findAll 测试 ====================

    @Test
    void findAll_shouldReturnAllFactors() {
        when(derivativeFactorJpaRepository.findAll()).thenReturn(List.of(mockEntity));

        List<DerivativeFactor> result = jpaDerivativeFactorRepository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("衍生ROE", result.get(0).name());
        verify(derivativeFactorJpaRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoFactors() {
        when(derivativeFactorJpaRepository.findAll()).thenReturn(List.of());

        List<DerivativeFactor> result = jpaDerivativeFactorRepository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== findById 测试 ====================

    @Test
    void findById_shouldReturnFactor_whenExists() {
        when(derivativeFactorJpaRepository.findById("d1")).thenReturn(Optional.of(mockEntity));

        Optional<DerivativeFactor> result = jpaDerivativeFactorRepository.findById("d1");

        assertTrue(result.isPresent());
        assertEquals("衍生ROE", result.get().name());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(derivativeFactorJpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<DerivativeFactor> result = jpaDerivativeFactorRepository.findById("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ==================== save 测试 ====================

    @Test
    void save_shouldCreateNew_whenIdIsNull() {
        LocalDateTime now = LocalDateTime.now();
        DerivativeFactor newFactor = new DerivativeFactor(null, "new_code", "新衍生因子",
                "admin", now, "描述", "formula", true);

        when(derivativeFactorJpaRepository.save(any(DerivativeFactorEntity.class)))
                .thenReturn(mockEntity);

        DerivativeFactor result = jpaDerivativeFactorRepository.save(newFactor);

        assertNotNull(result);
        verify(derivativeFactorJpaRepository, times(1)).save(any(DerivativeFactorEntity.class));
    }

    @Test
    void save_shouldUpdateExisting_whenIdIsNotNull() {
        when(derivativeFactorJpaRepository.save(any(DerivativeFactorEntity.class)))
                .thenReturn(mockEntity);

        DerivativeFactor result = jpaDerivativeFactorRepository.save(mockDomain);

        assertNotNull(result);
        verify(derivativeFactorJpaRepository, times(1)).save(any(DerivativeFactorEntity.class));
    }
}