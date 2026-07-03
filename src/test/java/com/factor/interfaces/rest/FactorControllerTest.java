package com.factor.interfaces.rest;

import com.factor.application.factor.FactorApplicationService;
import com.factor.common.api.ApiResponse;
import com.factor.common.model.PageResult;
import com.factor.domain.factor.*;
import com.factor.interfaces.rest.dto.FactorQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子控制器测试
 */
class FactorControllerTest {

    private FactorController controller;
    private FactorApplicationServiceStub factorService;

    @BeforeEach
    void setUp() {
        factorService = new FactorApplicationServiceStub();
        controller = new FactorController(factorService);
    }

    @Test
    void testCategories() {
        ApiResponse<List<FactorCategoryNode>> response = controller.categories();
        assertTrue(response.success());
        assertNotNull(response.data());
        assertTrue(response.data().size() > 0);
    }

    @Test
    void testFunds() {
        ApiResponse<List<FundInfo>> response = controller.funds(null, 100);
        assertTrue(response.success());
        assertNotNull(response.data());
    }

    @Test
    void testFundsWithKeyword() {
        ApiResponse<List<FundInfo>> response = controller.funds("易方达", 100);
        assertTrue(response.success());
    }

    @Test
    void testBaseFactors() {
        ApiResponse<PageResult<BaseFactor>> response = controller.baseFactors(null, 1, 10);
        assertTrue(response.success());
        assertNotNull(response.data());
    }

    @Test
    void testBaseFactorsWithCategory() {
        ApiResponse<PageResult<BaseFactor>> response = controller.baseFactors("cat-001", 1, 10);
        assertTrue(response.success());
    }

    @Test
    void testBaseFactorValues() {
        FactorQueryRequest request = new FactorQueryRequest("000001", "bf-1", null, null, 1, 10);
        ApiResponse<List<BaseFactorValue>> response = controller.baseFactorValues(request);
        assertTrue(response.success());
        assertNotNull(response.data());
    }

    @Test
    void testSaveBaseFactor() {
        BaseFactor factor = new BaseFactor(null, "new_factor", "新因子", "cat-001",
                "数值型", "%", "日频", "Wind", "逻辑", true, true, "描述");
        ApiResponse<BaseFactor> response = controller.saveBaseFactor(factor);
        assertTrue(response.success());
        assertNotNull(response.data());
    }

    @Test
    void testDerivedFactors() {
        ApiResponse<List<DerivativeFactor>> response = controller.derivedFactors();
        assertTrue(response.success());
        assertNotNull(response.data());
    }

    @Test
    void testCreateDerived() {
        DerivativeFactorCreateRequest request = new DerivativeFactorCreateRequest(
                "测试衍生因子",
                List.of(new DerivativeFactorCreateRequest.Item("bf-1", new BigDecimal("50")),
                        new DerivativeFactorCreateRequest.Item("bf-2", new BigDecimal("50"))),
                "f1 + f2"
        );
        ApiResponse<DerivativeFactor> response = controller.createDerived(request);
        assertTrue(response.success());
        assertNotNull(response.data());
    }

    @Test
    void testStyleFactors() {
        ApiResponse<List<StyleFactorDefinition>> response = controller.styleFactors();
        assertTrue(response.success());
        assertNotNull(response.data());
    }

    /**
     * FactorApplicationService 测试桩
     */
    private static class FactorApplicationServiceStub implements FactorApplicationService {

        @Override
        public List<FactorCategoryNode> categoryTree() {
            return List.of(new FactorCategoryNode("cat-1", null, "费率水平", 1, 1, "费率因子分类", true, List.of()));
        }

        @Override
        public List<FundInfo> funds() {
            return List.of(new FundInfo("000001", "易方达天天理财货币A", "天天理财A", "货币型", LocalDate.of(2010, 1, 1), "易方达基金", "张经理", "OPEN"));
        }

        @Override
        public PageResult<BaseFactor> listBaseFactors(String categoryId, long page, long size) {
            List<BaseFactor> items = List.of(
                    new BaseFactor("bf-1", "management_fee", "管理费率", "cat-001", "数值型", "%", "月度", "Wind", "逻辑", true, true, "管理费率")
            );
            return new PageResult<>(items, page, size, items.size());
        }

        @Override
        public Optional<BaseFactor> getBaseFactor(String id) {
            return Optional.of(new BaseFactor("bf-1", "management_fee", "管理费率", "cat-001", "数值型", "%", "月度", "Wind", "逻辑", true, true, "管理费率"));
        }

        @Override
        public BaseFactor saveBaseFactor(BaseFactor factor) {
            return new BaseFactor(UUID.randomUUID().toString(), factor.code(), factor.name(), factor.categoryId(), factor.dataType(), factor.unit(), factor.updateFrequency(), factor.dataSource(), factor.fetchLogic(), factor.enabled(), factor.derivable(), factor.description());
        }

        @Override
        public List<BaseFactorValue> baseFactorValues(FactorQueryCondition condition) { return List.of(); }

        @Override
        public DerivativeFactor createDerivativeFactor(DerivativeFactorCreateRequest request, String createdBy) {
            return new DerivativeFactor("df-new", "test", request.name(), createdBy, LocalDateTime.now(), request.formula(), request.formula(), true);
        }

        @Override
        public DerivativeFactor updateDerivativeFactor(String id, DerivativeFactorCreateRequest request, String updatedBy) {
            return new DerivativeFactor(id, "test", request.name(), updatedBy, LocalDateTime.now(), request.formula(), request.formula(), true);
        }

        @Override
        public void deleteDerivativeFactor(String id) {}

        @Override
        public List<DerivativeFactor> listDerivativeFactors() {
            return List.of(new DerivativeFactor("df-1", "fee_bundle", "费率组合因子", "system", LocalDateTime.now(), "组合因子", null, true));
        }

        @Override
        public List<DerivativeFactorValue> derivativeFactorValues(FactorQueryCondition condition) { return List.of(); }

        @Override
        public StyleFactorDefinition createStyleFactor(StyleFactorCreateRequest request, String createdBy) {
            return new StyleFactorDefinition("sf-new", request.name(), createdBy, LocalDateTime.now(), "组合生成", true);
        }

        @Override
        public StyleFactorDefinition updateStyleFactor(String id, StyleFactorCreateRequest request, String updatedBy) {
            return new StyleFactorDefinition(id, request.name(), updatedBy, LocalDateTime.now(), "更新", true);
        }

        @Override
        public void deleteStyleFactor(String id) {}

        @Override
        public List<StyleFactorDefinition> listStyleFactors() {
            return List.of(new StyleFactorDefinition("sf-1", "稳健风格因子", "system", LocalDateTime.now(), "稳健风格", true));
        }

        @Override
        public List<StyleFactorValue> styleFactorValues(FactorQueryCondition condition) { return List.of(); }
    }
}
