package com.factor.infrastructure.persistence;

import com.factor.domain.factor.Factor;
import com.factor.domain.factor.FactorCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子仓储内存实现测试
 */
class InMemoryFactorRepositoryTest {

    private InMemoryFactorRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFactorRepository();
    }

    @Test
    void testFindAll() {
        List<Factor> factors = repository.findAll();
        assertNotNull(factors);
        assertTrue(factors.size() > 0);
    }

    @Test
    void testFindByIdExisting() {
        Optional<Factor> factor = repository.findById("f1");
        assertTrue(factor.isPresent());
        assertEquals("annual_return", factor.get().code());
        assertEquals(FactorCategory.RETURN, factor.get().category());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Factor> factor = repository.findById("non-existent");
        assertFalse(factor.isPresent());
    }

    @Test
    void testFindByCategory() {
        List<Factor> returnFactors = repository.findByCategory(FactorCategory.RETURN);
        assertTrue(returnFactors.size() > 0);
        assertTrue(returnFactors.stream().allMatch(f -> f.category() == FactorCategory.RETURN));
    }

    @Test
    void testFindByCategoryRisk() {
        List<Factor> riskFactors = repository.findByCategory(FactorCategory.RISK);
        assertTrue(riskFactors.size() > 0);
    }

    @Test
    void testFindByCategoryNoMatch() {
        List<Factor> factors = repository.findByCategory(FactorCategory.DERIVED);
        assertEquals(0, factors.size());
    }

    @Test
    void testSaveNewFactor() {
        Factor newFactor = new Factor("f3", "sharpe_ratio", "夏普比率", FactorCategory.RETURN, "Wind",
                "夏普比率因子", new BigDecimal("1.5"), null, null, LocalDateTime.now(), LocalDateTime.now());

        Factor saved = repository.save(newFactor);
        assertEquals("f3", saved.id());

        Optional<Factor> found = repository.findById("f3");
        assertTrue(found.isPresent());
        assertEquals("sharpe_ratio", found.get().code());
    }

    @Test
    void testSaveUpdateExisting() {
        Factor updated = repository.save(new Factor("f1", "annual_return", "年化收益率更新",
                FactorCategory.RETURN, "Wind", "更新描述",
                new BigDecimal("0.15"), "%", null, LocalDateTime.now(), LocalDateTime.now()));

        assertEquals("年化收益率更新", updated.name());

        Optional<Factor> found = repository.findById("f1");
        assertTrue(found.isPresent());
        assertEquals("年化收益率更新", found.get().name());
    }

    @Test
    void testFindAllHasTwoFactors() {
        List<Factor> factors = repository.findAll();
        assertTrue(factors.size() >= 2);
    }

    @Test
    void testFindAllReturnsImmutable() {
        List<Factor> factors = repository.findAll();
        assertThrows(UnsupportedOperationException.class, () -> factors.add(null));
    }

    @Test
    void testFactorDetails() {
        Optional<Factor> f1 = repository.findById("f1");
        assertTrue(f1.isPresent());
        assertEquals("年化收益率", f1.get().name());
        assertEquals(new BigDecimal("0.1234"), f1.get().latestValue());
        assertEquals("%", f1.get().unit());

        Optional<Factor> f2 = repository.findById("f2");
        assertTrue(f2.isPresent());
        assertEquals("最大回撤", f2.get().name());
        assertEquals(new BigDecimal("-0.0812"), f2.get().latestValue());
    }
}
