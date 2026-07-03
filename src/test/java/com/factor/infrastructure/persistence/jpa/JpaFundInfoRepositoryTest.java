package com.factor.infrastructure.persistence.jpa;

import com.factor.domain.factor.FundInfo;
import com.factor.domain.factor.repository.FundInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaFundInfoRepositoryTest {

    @Mock
    private FundProfileJpaRepository fundProfileJpaRepository;

    @InjectMocks
    private JpaFundInfoRepository jpaFundInfoRepository;

    private FundProfileEntity mockEntity;

    @BeforeEach
    void setUp() {
        mockEntity = new FundProfileEntity();
        mockEntity.setFundCode("000001");
        mockEntity.setFundName("华夏成长");
        mockEntity.setFundType("股票型");
        mockEntity.setSetupDate(LocalDate.of(2000, 1, 1));
        mockEntity.setCompanyName("华夏基金");
        mockEntity.setManagerName("张三");
    }

    // ==================== findAll 测试 ====================

    @Test
    void findAll_shouldReturnAllFunds() {
        when(fundProfileJpaRepository.findAll()).thenReturn(List.of(mockEntity));

        List<FundInfo> result = jpaFundInfoRepository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("000001", result.get(0).fundCode());
        assertEquals("华夏成长", result.get(0).fundName());
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoFunds() {
        when(fundProfileJpaRepository.findAll()).thenReturn(List.of());

        List<FundInfo> result = jpaFundInfoRepository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldMapAllFieldsCorrectly() {
        when(fundProfileJpaRepository.findAll()).thenReturn(List.of(mockEntity));

        List<FundInfo> result = jpaFundInfoRepository.findAll();

        FundInfo fund = result.get(0);
        assertEquals("000001", fund.fundCode());
        assertEquals("华夏成长", fund.fundName());
        assertEquals("股票型", fund.fundType());
        assertEquals(LocalDate.of(2000, 1, 1), fund.establishmentDate());
        assertEquals("华夏基金", fund.issuer());
        assertEquals("张三", fund.fundManager());
        assertEquals("OPEN", fund.status());
    }

    // ==================== findByCode 测试 ====================

    @Test
    void findByCode_shouldReturnFund_whenExists() {
        when(fundProfileJpaRepository.findById("000001")).thenReturn(Optional.of(mockEntity));

        Optional<FundInfo> result = jpaFundInfoRepository.findByCode("000001");

        assertTrue(result.isPresent());
        assertEquals("000001", result.get().fundCode());
        assertEquals("华夏成长", result.get().fundName());
    }

    @Test
    void findByCode_shouldReturnEmpty_whenNotExists() {
        when(fundProfileJpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<FundInfo> result = jpaFundInfoRepository.findByCode("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCode_shouldHandleNullFields() {
        mockEntity.setManagerName(null);
        when(fundProfileJpaRepository.findById("000001")).thenReturn(Optional.of(mockEntity));

        Optional<FundInfo> result = jpaFundInfoRepository.findByCode("000001");

        assertTrue(result.isPresent());
        assertNull(result.get().fundManager());
    }
}
