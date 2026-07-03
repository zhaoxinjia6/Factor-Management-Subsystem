package com.factor.infrastructure.persistence.jpa;

import com.factor.domain.factor.BaseFactor;
import com.factor.domain.factor.repository.BaseFactorRepository;
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
class JpaBaseFactorRepositoryTest {

    @Mock
    private BaseFactorJpaRepository baseFactorJpaRepository;

    @InjectMocks
    private JpaBaseFactorRepository jpaBaseFactorRepository;

    private BaseFactorEntity mockEntity;
    private BaseFactor mockDomain;

    @BeforeEach
    void setUp() {
        mockEntity = new BaseFactorEntity();
        mockEntity.setId("f1");
        mockEntity.setCode("ROE");
        mockEntity.setName("净资产收益率");
        mockEntity.setCategoryId("c1");
        mockEntity.setDataType("wind");
        mockEntity.setUnit("%");
        mockEntity.setEnabled(true);
        mockEntity.setDerivable(true);

        mockDomain = new BaseFactor("f1", "ROE", "净资产收益率", "c1", "wind", "净资产收益率",
                "daily", "wind", null, true, true, null);
    }

    // ==================== findAll 测试 ====================

    @Test
    void findAll_shouldReturnAllFactors() {
        when(baseFactorJpaRepository.findAll()).thenReturn(List.of(mockEntity));

        List<BaseFactor> result = jpaBaseFactorRepository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ROE", result.get(0).code());
        verify(baseFactorJpaRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoFactors() {
        when(baseFactorJpaRepository.findAll()).thenReturn(List.of());

        List<BaseFactor> result = jpaBaseFactorRepository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== findById 测试 ====================

    @Test
    void findById_shouldReturnFactor_whenExists() {
        when(baseFactorJpaRepository.findById("f1")).thenReturn(Optional.of(mockEntity));

        Optional<BaseFactor> result = jpaBaseFactorRepository.findById("f1");

        assertTrue(result.isPresent());
        assertEquals("ROE", result.get().code());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(baseFactorJpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<BaseFactor> result = jpaBaseFactorRepository.findById("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ==================== save 测试 ====================

    @Test
    void save_shouldCreateNew_whenIdIsNull() {
        BaseFactor newFactor = new BaseFactor(null, "ROE", "净资产收益率", "c1", "wind", "净资产收益率",
                "daily", "wind", null, true, true, null);

        when(baseFactorJpaRepository.save(any(BaseFactorEntity.class)))
                .thenReturn(mockEntity);

        BaseFactor result = jpaBaseFactorRepository.save(newFactor);

        assertNotNull(result);
        verify(baseFactorJpaRepository, times(1)).save(any(BaseFactorEntity.class));
    }

    @Test
    void save_shouldUpdateExisting_whenIdIsNotNull() {
        when(baseFactorJpaRepository.save(any(BaseFactorEntity.class)))
                .thenReturn(mockEntity);

        BaseFactor result = jpaBaseFactorRepository.save(mockDomain);

        assertNotNull(result);
        verify(baseFactorJpaRepository, times(1)).save(any(BaseFactorEntity.class));
    }
}