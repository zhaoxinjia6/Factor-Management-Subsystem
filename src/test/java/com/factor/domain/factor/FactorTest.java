package com.factor.domain.factor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子领域模型测试
 */
class FactorTest {

    @Test
    void testCreateFactor() {
        Factor factor = new Factor(
                "f1", "annual_return", "年化收益率",
                FactorCategory.RETURN, "Wind", "基金年化收益率",
                new BigDecimal("0.1234"), "%", null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        assertEquals("f1", factor.id());
        assertEquals("annual_return", factor.code());
        assertEquals("年化收益率", factor.name());
        assertEquals(FactorCategory.RETURN, factor.category());
        assertEquals("Wind", factor.source());
        assertEquals("基金年化收益率", factor.description());
        assertEquals(new BigDecimal("0.1234"), factor.latestValue());
        assertEquals("%", factor.unit());
    }

    @Test
    void testCreateRiskFactor() {
        Factor factor = new Factor(
                "f2", "max_drawdown", "最大回撤",
                FactorCategory.RISK, "Wind", "基金最大回撤",
                new BigDecimal("-0.0812"), "%", null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        assertEquals(FactorCategory.RISK, factor.category());
        assertEquals(new BigDecimal("-0.0812"), factor.latestValue());
    }

    @Test
    void testFactorWithMetadata() {
        Map<String, Object> metadata = Map.of("source", "Wind", "frequency", "daily");
        Factor factor = new Factor(
                "f3", "test", "测试因子",
                FactorCategory.CUSTOM, "内部系统", "测试",
                BigDecimal.ZERO, null, metadata,
                LocalDateTime.now(), LocalDateTime.now()
        );

        assertNotNull(factor.metadata());
        assertEquals("Wind", factor.metadata().get("source"));
    }

    @Test
    void testFactorWithNullValue() {
        Factor factor = new Factor(
                "f4", "null_val", "空值因子",
                FactorCategory.STYLE, "系统", null,
                null, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        assertNull(factor.latestValue());
        assertNull(factor.unit());
        assertNull(factor.metadata());
    }

    @Test
    void testFactorCategoryEnum() {
        assertEquals(7, FactorCategory.values().length);
        assertTrue(FactorCategory.valueOf("RETURN") == FactorCategory.RETURN);
        assertTrue(FactorCategory.valueOf("RISK") == FactorCategory.RISK);
        assertTrue(FactorCategory.valueOf("STYLE") == FactorCategory.STYLE);
        assertTrue(FactorCategory.valueOf("DERIVED") == FactorCategory.DERIVED);
    }

    @Test
    void testFactorEquality() {
        LocalDateTime now = LocalDateTime.now();
        Factor factor1 = new Factor("f1", "code1", "因子1", FactorCategory.RETURN, "Wind", "描述",
                BigDecimal.ONE, "%", null, now, now);
        Factor factor2 = new Factor("f1", "code1", "因子1", FactorCategory.RETURN, "Wind", "描述",
                BigDecimal.ONE, "%", null, now, now);

        assertEquals(factor1, factor2);
        assertEquals(factor1.hashCode(), factor2.hashCode());
    }

    @Test
    void testFactorToString() {
        Factor factor = new Factor("f1", "annual_return", "年化收益率",
                FactorCategory.RETURN, "Wind", "描述",
                new BigDecimal("0.12"), "%", null, LocalDateTime.now(), LocalDateTime.now());

        String str = factor.toString();
        assertNotNull(str);
        assertTrue(str.contains("annual_return"));
        assertTrue(str.contains("年化收益率"));
    }

    @Test
    void testDifferentFactorCategories() {
        Factor returnFactor = new Factor("f1", "ret", "收益", FactorCategory.RETURN, "Wind", "", null, null, null, LocalDateTime.now(), LocalDateTime.now());
        Factor riskFactor = new Factor("f2", "risk", "风险", FactorCategory.RISK, "Wind", "", null, null, null, LocalDateTime.now(), LocalDateTime.now());
        Factor liquidityFactor = new Factor("f3", "liq", "流动性", FactorCategory.LIQUIDITY, "Wind", "", null, null, null, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(FactorCategory.RETURN, returnFactor.category());
        assertEquals(FactorCategory.RISK, riskFactor.category());
        assertEquals(FactorCategory.LIQUIDITY, liquidityFactor.category());
    }
}
