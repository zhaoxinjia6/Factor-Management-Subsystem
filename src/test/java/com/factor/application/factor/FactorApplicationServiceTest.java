package com.factor.application.factor;

import com.factor.common.model.PageResult;
import com.factor.domain.factor.*;
import com.factor.domain.factor.repository.*;
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
 * 因子应用服务测试
 */
class FactorApplicationServiceTest {

    private FactorApplicationService factorService;
    private FactorCategoryRepositoryStub categoryRepo;
    private FundInfoRepositoryStub fundInfoRepo;
    private BaseFactorRepositoryStub baseFactorRepo;
    private BaseFactorValueRepositoryStub baseFactorValueRepo;
    private DerivativeFactorRepositoryStub derivativeFactorRepo;
    private DerivativeFactorItemRepositoryStub derivativeFactorItemRepo;
    private DerivativeFactorValueRepositoryStub derivativeFactorValueRepo;
    private StyleFactorRepositoryStub styleFactorRepo;
    private StyleFactorItemRepositoryStub styleFactorItemRepo;
    private StyleFactorValueRepositoryStub styleFactorValueRepo;

    @BeforeEach
    void setUp() {
        categoryRepo = new FactorCategoryRepositoryStub();
        fundInfoRepo = new FundInfoRepositoryStub();
        baseFactorRepo = new BaseFactorRepositoryStub();
        baseFactorValueRepo = new BaseFactorValueRepositoryStub();
        derivativeFactorRepo = new DerivativeFactorRepositoryStub();
        derivativeFactorItemRepo = new DerivativeFactorItemRepositoryStub();
        derivativeFactorValueRepo = new DerivativeFactorValueRepositoryStub();
        styleFactorRepo = new StyleFactorRepositoryStub();
        styleFactorItemRepo = new StyleFactorItemRepositoryStub();
        styleFactorValueRepo = new StyleFactorValueRepositoryStub();

        factorService = new FactorApplicationServiceImpl(
                categoryRepo, fundInfoRepo, baseFactorRepo, baseFactorValueRepo,
                derivativeFactorRepo, derivativeFactorItemRepo, derivativeFactorValueRepo,
                styleFactorRepo, styleFactorItemRepo, styleFactorValueRepo
        );
    }

    @Test
    void testCategoryTree() {
        List<FactorCategoryNode> tree = factorService.categoryTree();
        assertNotNull(tree);
        assertTrue(tree.size() > 0);
    }

    @Test
    void testFunds() {
        List<FundInfo> funds = factorService.funds();
        assertNotNull(funds);
        assertTrue(funds.size() > 0);
    }

    @Test
    void testListBaseFactors() {
        PageResult<BaseFactor> result = factorService.listBaseFactors(null, 1, 10);
        assertNotNull(result);
        assertTrue(result.items().size() > 0);
        assertTrue(result.total() > 0);
    }

    @Test
    void testListBaseFactorsWithCategoryFilter() {
        PageResult<BaseFactor> result = factorService.listBaseFactors("cat-001", 1, 10);
        assertNotNull(result);
    }

    @Test
    void testGetBaseFactor() {
        Optional<BaseFactor> factor = factorService.getBaseFactor("bf-1");
        assertTrue(factor.isPresent());
        assertEquals("management_fee", factor.get().code());
    }

    @Test
    void testGetBaseFactorNotFound() {
        Optional<BaseFactor> factor = factorService.getBaseFactor("non-existent");
        assertFalse(factor.isPresent());
    }

    @Test
    void testSaveBaseFactor() {
        BaseFactor newFactor = new BaseFactor(null, "new_factor", "新因子", "cat-001",
                "数值型", "%", "日频", "Wind", "逻辑", true, true, "描述");
        BaseFactor saved = factorService.saveBaseFactor(newFactor);
        assertNotNull(saved.id());
    }

    @Test
    void testBaseFactorValues() {
        FactorQueryCondition condition = new FactorQueryCondition("000001", "bf-1", null, null, 1, 10);
        List<BaseFactorValue> values = factorService.baseFactorValues(condition);
        assertNotNull(values);
    }

    @Test
    void testListDerivativeFactors() {
        List<DerivativeFactor> factors = factorService.listDerivativeFactors();
        assertNotNull(factors);
    }

    @Test
    void testListStyleFactors() {
        List<StyleFactorDefinition> factors = factorService.listStyleFactors();
        assertNotNull(factors);
    }

    // ── Stub implementations ──

    private static class FactorCategoryRepositoryStub implements FactorCategoryRepository {
        @Override public List<FactorCategoryNode> findTree() {
            return List.of(new FactorCategoryNode("cat-1", null, "费率水平", 1, 1, "费率", true, List.of()));
        }
        @Override public Optional<FactorCategoryNode> findById(String id) { return Optional.empty(); }
        @Override public FactorCategoryNode save(FactorCategoryNode category) { return category; }
    }

    private static class FundInfoRepositoryStub implements FundInfoRepository {
        @Override public List<FundInfo> findAll() {
            return List.of(new FundInfo("000001", "易方达天天理财货币A", "天天理财A", "货币型", LocalDate.of(2010, 1, 1), "易方达基金", "张经理", "OPEN"));
        }
        @Override public Optional<FundInfo> findByCode(String fundCode) { return Optional.empty(); }
    }

    private static class BaseFactorRepositoryStub implements BaseFactorRepository {
        private final List<BaseFactor> storage = new java.util.ArrayList<>(List.of(
                new BaseFactor("bf-1", "management_fee", "管理费率", "cat-001", "数值型", "%", "月度", "Wind", "基金公告", true, true, "管理费率")
        ));
        @Override public List<BaseFactor> findAll() { return List.copyOf(storage); }
        @Override public Optional<BaseFactor> findById(String id) {
            return storage.stream().filter(f -> f.id().equals(id)).findFirst();
        }
        @Override public BaseFactor save(BaseFactor factor) {
            BaseFactor stored = factor.id() == null ? new BaseFactor(UUID.randomUUID().toString(), factor.code(), factor.name(), factor.categoryId(), factor.dataType(), factor.unit(), factor.updateFrequency(), factor.dataSource(), factor.fetchLogic(), factor.enabled(), factor.derivable(), factor.description()) : factor;
            storage.removeIf(f -> f.id().equals(stored.id()));
            storage.add(stored);
            return stored;
        }
    }

    private static class BaseFactorValueRepositoryStub implements BaseFactorValueRepository {
        @Override public List<BaseFactorValue> query(String fundCode, String factorId) { return List.of(); }
        @Override public List<BaseFactorValue> query(String fundCode, String factorId, LocalDate startDate, LocalDate endDate) {
            return List.of();
        }
    }

    private static class DerivativeFactorRepositoryStub implements DerivativeFactorRepository {
        private final List<DerivativeFactor> storage = new java.util.ArrayList<>(List.of(
                new DerivativeFactor("df-1", "fee_bundle", "费率组合因子", "system", LocalDateTime.now(), "组合因子", null, true)
        ));
        @Override public List<DerivativeFactor> findAll() { return List.copyOf(storage); }
        @Override public Optional<DerivativeFactor> findById(String id) {
            return storage.stream().filter(f -> f.id().equals(id)).findFirst();
        }
        @Override public DerivativeFactor save(DerivativeFactor factor) {
            DerivativeFactor stored = factor.id() == null
                    ? new DerivativeFactor(UUID.randomUUID().toString(), factor.code(), factor.name(), factor.createdBy(), factor.createdAt(), factor.description(), factor.formula(), factor.enabled())
                    : factor;
            storage.removeIf(f -> f.id().equals(stored.id()));
            storage.add(stored);
            return stored;
        }
    }

    private static class DerivativeFactorItemRepositoryStub implements DerivativeFactorItemRepository {
        private final List<DerivativeFactorItem> storage = new java.util.ArrayList<>();
        @Override public List<DerivativeFactorItem> findByDerivativeFactorId(String id) { return List.of(); }
        @Override public List<DerivativeFactorItem> saveAll(List<DerivativeFactorItem> items) {
            storage.addAll(items);
            return items;
        }
    }

    private static class DerivativeFactorValueRepositoryStub implements DerivativeFactorValueRepository {
        @Override public List<DerivativeFactorValue> query(String fundCode, String factorId) { return List.of(); }
    }

    private static class StyleFactorRepositoryStub implements StyleFactorRepository {
        private final List<StyleFactorDefinition> storage = new java.util.ArrayList<>(List.of(
                new StyleFactorDefinition("sf-1", "稳健风格因子", "system", LocalDateTime.now(), "稳健风格", true)
        ));
        @Override public List<StyleFactorDefinition> findAll() { return List.copyOf(storage); }
        @Override public Optional<StyleFactorDefinition> findById(String id) {
            return storage.stream().filter(f -> f.id().equals(id)).findFirst();
        }
        @Override public StyleFactorDefinition save(StyleFactorDefinition factor) {
            StyleFactorDefinition stored = factor.id() == null
                    ? new StyleFactorDefinition(UUID.randomUUID().toString(), factor.name(), factor.createdBy(), factor.createdAt(), factor.description(), factor.enabled())
                    : factor;
            storage.removeIf(f -> f.id().equals(stored.id()));
            storage.add(stored);
            return stored;
        }
    }

    private static class StyleFactorItemRepositoryStub implements StyleFactorItemRepository {
        private final List<StyleFactorItem> storage = new java.util.ArrayList<>();
        @Override public List<StyleFactorItem> findByStyleFactorId(String id) { return List.of(); }
        @Override public List<StyleFactorItem> saveAll(List<StyleFactorItem> items) {
            storage.addAll(items);
            return items;
        }
    }

    private static class StyleFactorValueRepositoryStub implements StyleFactorValueRepository {
        @Override public List<StyleFactorValue> query(String fundCode, String factorId) { return List.of(); }
    }
}
