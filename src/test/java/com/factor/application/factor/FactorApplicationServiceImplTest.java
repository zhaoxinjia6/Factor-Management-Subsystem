package com.factor.application.factor;

import com.factor.common.exception.BusinessException;
import com.factor.common.model.PageResult;
import com.factor.domain.factor.BaseFactor;
import com.factor.domain.factor.BaseFactorValue;
import com.factor.domain.factor.DerivativeFactor;
import com.factor.domain.factor.DerivativeFactorCreateRequest;
import com.factor.domain.factor.DerivativeFactorValue;
import com.factor.domain.factor.FactorCategoryNode;
import com.factor.domain.factor.FactorQueryCondition;
import com.factor.domain.factor.FundInfo;
import com.factor.domain.factor.StyleFactorCreateRequest;
import com.factor.domain.factor.StyleFactorDefinition;
import com.factor.domain.factor.StyleFactorValue;
import com.factor.domain.factor.repository.BaseFactorRepository;
import com.factor.domain.factor.repository.BaseFactorValueRepository;
import com.factor.domain.factor.repository.DerivativeFactorItemRepository;
import com.factor.domain.factor.repository.DerivativeFactorRepository;
import com.factor.domain.factor.repository.DerivativeFactorValueRepository;
import com.factor.domain.factor.repository.FactorCategoryRepository;
import com.factor.domain.factor.repository.FundInfoRepository;
import com.factor.domain.factor.repository.StyleFactorItemRepository;
import com.factor.domain.factor.repository.StyleFactorRepository;
import com.factor.domain.factor.repository.StyleFactorValueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactorApplicationServiceImplTest {

    @Mock private FactorCategoryRepository factorCategoryRepository;
    @Mock private FundInfoRepository fundInfoRepository;
    @Mock private BaseFactorRepository baseFactorRepository;
    @Mock private BaseFactorValueRepository baseFactorValueRepository;
    @Mock private DerivativeFactorRepository derivativeFactorRepository;
    @Mock private DerivativeFactorItemRepository derivativeFactorItemRepository;
    @Mock private DerivativeFactorValueRepository derivativeFactorValueRepository;
    @Mock private StyleFactorRepository styleFactorRepository;
    @Mock private StyleFactorItemRepository styleFactorItemRepository;
    @Mock private StyleFactorValueRepository styleFactorValueRepository;

    @InjectMocks
    private FactorApplicationServiceImpl factorService;

    private BaseFactor mockBaseFactor;

    @BeforeEach
    void setUp() {
        mockBaseFactor = new BaseFactor(
                "f1", "ROE", "净资产收益率", "c1", "wind", "净资产收益率",
                "15.5", "%", null, true, true, null
        );
    }

    // ==================== categoryTree 测试 ====================

    @Test
    void categoryTree_shouldReturnTree() {
        List<FactorCategoryNode> mockTree = List.of(
                new FactorCategoryNode("c1", "收益类", null, 1, 0, null, false, null)
        );
        when(factorCategoryRepository.findTree()).thenReturn(mockTree);

        List<FactorCategoryNode> result = factorService.categoryTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(factorCategoryRepository, times(1)).findTree();
    }

    // ==================== funds 测试 ====================

    @Test
    void funds_shouldReturnAllFunds() {
        List<FundInfo> mockFunds = List.of(
                new FundInfo("000001", "华夏成长", "华夏基金", "股票型", LocalDate.now(), "Y", "主动", "权益")
        );
        when(fundInfoRepository.findAll()).thenReturn(mockFunds);

        List<FundInfo> result = factorService.funds();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("000001", result.get(0).fundCode());
    }

    // ==================== listBaseFactors 测试 ====================

    @Test
    void listBaseFactors_shouldReturnPagedResults() {
        List<BaseFactor> factors = List.of(mockBaseFactor);
        when(baseFactorRepository.findAll()).thenReturn(factors);

        PageResult<BaseFactor> result = factorService.listBaseFactors(null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.total());
        assertEquals(1, result.items().size());
    }

    @Test
    void listBaseFactors_shouldFilterByCategory() {
        List<BaseFactor> factors = List.of(mockBaseFactor);
        when(baseFactorRepository.findAll()).thenReturn(factors);

        PageResult<BaseFactor> result = factorService.listBaseFactors("c1", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.items().size());
    }

    @Test
    void listBaseFactors_shouldReturnEmpty_whenNoMatch() {
        when(baseFactorRepository.findAll()).thenReturn(List.of());

        PageResult<BaseFactor> result = factorService.listBaseFactors("nonexistent", 1, 10);

        assertNotNull(result);
        assertEquals(0, result.total());
        assertTrue(result.items().isEmpty());
    }

    // ==================== getBaseFactor 测试 ====================

    @Test
    void getBaseFactor_shouldReturnFactor_whenExists() {
        when(baseFactorRepository.findById("f1")).thenReturn(Optional.of(mockBaseFactor));

        Optional<BaseFactor> result = factorService.getBaseFactor("f1");

        assertTrue(result.isPresent());
        assertEquals("ROE", result.get().code());
    }

    @Test
    void getBaseFactor_shouldReturnEmpty_whenNotExists() {
        when(baseFactorRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<BaseFactor> result = factorService.getBaseFactor("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ==================== saveBaseFactor 测试 ====================

    @Test
    void saveBaseFactor_shouldSaveAndReturn() {
        when(baseFactorRepository.save(any(BaseFactor.class))).thenReturn(mockBaseFactor);

        BaseFactor result = factorService.saveBaseFactor(mockBaseFactor);

        assertNotNull(result);
        verify(baseFactorRepository, times(1)).save(mockBaseFactor);
    }

    // ==================== baseFactorValues 测试 ====================

    @Test
    void baseFactorValues_shouldReturnValues() {
        FactorQueryCondition condition = new FactorQueryCondition("000001", "f1", null, null, 0, 0);
        List<BaseFactorValue> mockValues = List.of(
                new BaseFactorValue("000001", "f1", "daily", LocalDate.now(), BigDecimal.valueOf(10.0), LocalDateTime.now())
        );
        when(baseFactorValueRepository.query(any(), any(), any(), any())).thenReturn(mockValues);

        List<BaseFactorValue> result = factorService.baseFactorValues(condition);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== createDerivativeFactor 测试 ====================

    @Test
    void createDerivativeFactor_shouldCreate_whenWeightSumIs100() {
        DerivativeFactorCreateRequest request = new DerivativeFactorCreateRequest(
                "测试衍生因子",
                List.of(
                        new DerivativeFactorCreateRequest.Item("f1", BigDecimal.valueOf(50)),
                        new DerivativeFactorCreateRequest.Item("f2", BigDecimal.valueOf(50))
                ),
                "f1*0.5+f2*0.5"
        );

        DerivativeFactor mockDerivative = new DerivativeFactor(
                "d1", "derived_roe", "衍生ROE", "admin", LocalDateTime.now(),
                "公式: f1*0.5+f2*0.5", "f1*0.5+f2*0.5", true
        );

        when(derivativeFactorRepository.save(any(DerivativeFactor.class))).thenReturn(mockDerivative);
        when(derivativeFactorItemRepository.saveAll(anyList())).thenReturn(List.of());

        DerivativeFactor result = factorService.createDerivativeFactor(request, "admin");

        assertNotNull(result);
        verify(derivativeFactorRepository, times(1)).save(any(DerivativeFactor.class));
        verify(derivativeFactorItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createDerivativeFactor_shouldThrowException_whenWeightSumNot100() {
        DerivativeFactorCreateRequest request = new DerivativeFactorCreateRequest(
                "测试衍生因子",
                List.of(
                        new DerivativeFactorCreateRequest.Item("f1", BigDecimal.valueOf(30)),
                        new DerivativeFactorCreateRequest.Item("f2", BigDecimal.valueOf(30))
                ),
                "f1*0.3+f2*0.3"
        );

        BusinessException exception = assertThrows(BusinessException.class,
                () -> factorService.createDerivativeFactor(request, "admin"));

        assertNotNull(exception.getMessage());
        verify(derivativeFactorRepository, never()).save(any());
    }

    // ==================== listDerivativeFactors 测试 ====================

    @Test
    void listDerivativeFactors_shouldReturnAll() {
        DerivativeFactor mockDerivative = new DerivativeFactor(
                "d1", "derived_roe", "衍生ROE", "admin", LocalDateTime.now(),
                "公式: ROE * 1.2", "ROE * 1.2", true
        );
        when(derivativeFactorRepository.findAll()).thenReturn(List.of(mockDerivative));

        List<DerivativeFactor> result = factorService.listDerivativeFactors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("衍生ROE", result.get(0).name());
    }

    // ==================== derivativeFactorValues 测试 ====================

    @Test
    void derivativeFactorValues_shouldReturnValues() {
        FactorQueryCondition condition = new FactorQueryCondition("000001", "d1", null, null, 0, 0);
        List<DerivativeFactorValue> mockValues = List.of(
                new DerivativeFactorValue("000001", "d1", "daily", LocalDate.now(), BigDecimal.valueOf(12.0), LocalDateTime.now())
        );
        when(derivativeFactorValueRepository.query(any(), any(), any(), any())).thenReturn(mockValues);

        List<DerivativeFactorValue> result = factorService.derivativeFactorValues(condition);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== createStyleFactor 测试 ====================

    @Test
    void createStyleFactor_shouldCreate_whenWeightSumIs100() {
        StyleFactorCreateRequest request = new StyleFactorCreateRequest(
                "测试风格因子",
                List.of(
                        new StyleFactorCreateRequest.Item("d1", BigDecimal.valueOf(60)),
                        new StyleFactorCreateRequest.Item("d2", BigDecimal.valueOf(40))
                )
        );

        StyleFactorDefinition mockStyle = new StyleFactorDefinition(
                "s1", "成长风格", "admin", LocalDateTime.now(), "成长风格因子", true
        );

        when(styleFactorRepository.save(any(StyleFactorDefinition.class))).thenReturn(mockStyle);
        when(styleFactorItemRepository.saveAll(anyList())).thenReturn(List.of());

        StyleFactorDefinition result = factorService.createStyleFactor(request, "admin");

        assertNotNull(result);
        verify(styleFactorRepository, times(1)).save(any(StyleFactorDefinition.class));
        verify(styleFactorItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createStyleFactor_shouldThrowException_whenWeightSumNot100() {
        StyleFactorCreateRequest request = new StyleFactorCreateRequest(
                "测试风格因子",
                List.of(
                        new StyleFactorCreateRequest.Item("d1", BigDecimal.valueOf(30)),
                        new StyleFactorCreateRequest.Item("d2", BigDecimal.valueOf(30))
                )
        );

        BusinessException exception = assertThrows(BusinessException.class,
                () -> factorService.createStyleFactor(request, "admin"));

        assertNotNull(exception.getMessage());
        verify(styleFactorRepository, never()).save(any());
    }

    // ==================== listStyleFactors 测试 ====================

    @Test
    void listStyleFactors_shouldReturnAll() {
        StyleFactorDefinition mockStyle = new StyleFactorDefinition(
                "s1", "成长风格", "admin", LocalDateTime.now(), "成长风格因子", true
        );
        when(styleFactorRepository.findAll()).thenReturn(List.of(mockStyle));

        List<StyleFactorDefinition> result = factorService.listStyleFactors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("成长风格", result.get(0).name());
    }

    // ==================== styleFactorValues 测试 ====================

    @Test
    void styleFactorValues_shouldReturnValues() {
        FactorQueryCondition condition = new FactorQueryCondition("000001", "s1", null, null, 0, 0);
        List<StyleFactorValue> mockValues = List.of(
                new StyleFactorValue("000001", "s1", "daily", LocalDate.now(), BigDecimal.valueOf(20.0), LocalDateTime.now())
        );
        when(styleFactorValueRepository.query(any(), any(), any(), any())).thenReturn(mockValues);

        List<StyleFactorValue> result = factorService.styleFactorValues(condition);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}