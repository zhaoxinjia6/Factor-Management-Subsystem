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

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子控制器全量测试
 */
class FactorControllerTest {

    private FactorController controller;
    private FactorApplicationServiceStub service;

    @BeforeEach
    void setUp() {
        service = new FactorApplicationServiceStub();
        controller = new FactorController(service);
    }

    @Test
    void testCategories() {
        ApiResponse<List<FactorCategoryNode>> resp = controller.categories();
        assertTrue(resp.success());
        assertNotNull(resp.data());
    }

    @Test
    void testFunds() {
        ApiResponse<List<FundInfo>> resp = controller.funds(null, 100);
        assertTrue(resp.success());
    }

    @Test
    void testFundsWithKeyword() {
        service.funds = List.of(
            new FundInfo("000001", "测试基金", "测试", "股票型", null, "基金公司", "经理", "正常")
        );
        ApiResponse<List<FundInfo>> resp = controller.funds("测试", 100);
        assertEquals(1, resp.data().size());
    }

    @Test
    void testBaseFactors() {
        ApiResponse<PageResult<BaseFactor>> resp = controller.baseFactors(null, 1, 10);
        assertTrue(resp.success());
    }

    @Test
    void testCreateDerived() {
        var req = new DerivativeFactorCreateRequest("测试因子",
            List.of(new DerivativeFactorCreateRequest.Item("bf1", BigDecimal.valueOf(50)),
                    new DerivativeFactorCreateRequest.Item("bf2", BigDecimal.valueOf(50))),
            "a+b");
        ApiResponse<DerivativeFactor> resp = controller.createDerived(req);
        assertTrue(resp.success());
        assertNotNull(resp.data());
    }

    @Test
    void testCreateStyle() {
        var req = new StyleFactorCreateRequest("风格因子",
            List.of(new StyleFactorCreateRequest.Item("df1", BigDecimal.valueOf(100))));
        ApiResponse<StyleFactorDefinition> resp = controller.createStyle(req);
        assertTrue(resp.success());
    }

    @Test
    void testDeleteDerived() {
        ApiResponse<Void> resp = controller.deleteDerived("test-id");
        assertTrue(resp.success());
    }

    @Test
    void testDeleteStyle() {
        ApiResponse<Void> resp = controller.deleteStyle("test-id");
        assertTrue(resp.success());
    }

    @Test
    void testDerivedFactors() {
        ApiResponse<List<DerivativeFactor>> resp = controller.derivedFactors();
        assertTrue(resp.success());
    }

    @Test
    void testStyleFactors() {
        ApiResponse<List<StyleFactorDefinition>> resp = controller.styleFactors();
        assertTrue(resp.success());
    }

    @Test
    void testBaseFactorValues() {
        var req = new FactorQueryRequest("000001", "bf1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), 1, 10);
        ApiResponse<List<BaseFactorValue>> resp = controller.baseFactorValues(req);
        assertTrue(resp.success());
    }

    @Test
    void testSaveBaseFactor() {
        BaseFactor bf = new BaseFactor("bf1", "code1", "管理费率", "cat1", "decimal", "%", "daily", "system", "api", true, true, "费率因子");
        ApiResponse<BaseFactor> resp = controller.saveBaseFactor(bf);
        assertTrue(resp.success());
    }

    /** FactorApplicationService 桩 */
    private static class FactorApplicationServiceStub implements FactorApplicationService {
        List<FundInfo> funds = List.of();

        @Override public List<FactorCategoryNode> categoryTree() { return List.of(); }
        @Override public List<FundInfo> funds() { return funds; }
        @Override public PageResult<BaseFactor> listBaseFactors(String c, long p, long s) {
            return new PageResult<>(List.of(), p, s, 0);
        }
        @Override public Optional<BaseFactor> getBaseFactor(String id) { return Optional.empty(); }
        @Override public BaseFactor saveBaseFactor(BaseFactor f) { return f; }
        @Override public List<BaseFactorValue> baseFactorValues(FactorQueryCondition c) { return List.of(); }
        @Override public DerivativeFactor createDerivativeFactor(DerivativeFactorCreateRequest r, String u) {
            return new DerivativeFactor("d1", "c", r.name(), u, LocalDateTime.now(), "d", "f", true);
        }
        @Override public DerivativeFactor updateDerivativeFactor(String id, DerivativeFactorCreateRequest r, String u) {
            return new DerivativeFactor(id, "c", r.name(), u, LocalDateTime.now(), "d", "f", true);
        }
        @Override public void deleteDerivativeFactor(String id) {}
        @Override public List<DerivativeFactor> listDerivativeFactors() { return List.of(); }
        @Override public List<DerivativeFactorValue> derivativeFactorValues(FactorQueryCondition c) { return List.of(); }
        @Override public StyleFactorDefinition createStyleFactor(StyleFactorCreateRequest r, String u) {
            return new StyleFactorDefinition("s1", r.name(), u, LocalDateTime.now(), "d", true);
        }
        @Override public StyleFactorDefinition updateStyleFactor(String id, StyleFactorCreateRequest r, String u) {
            return new StyleFactorDefinition(id, r.name(), u, LocalDateTime.now(), "d", true);
        }
        @Override public void deleteStyleFactor(String id) {}
        @Override public List<StyleFactorDefinition> listStyleFactors() { return List.of(); }
        @Override public List<StyleFactorValue> styleFactorValues(FactorQueryCondition c) { return List.of(); }
    }
}
