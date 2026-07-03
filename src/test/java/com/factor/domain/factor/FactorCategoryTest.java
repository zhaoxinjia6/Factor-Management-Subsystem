package com.factor.domain.factor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子分类枚举测试
 */
class FactorCategoryTest {

    @Test
    void testAllCategories() {
        assertEquals(7, FactorCategory.values().length);
    }

    @Test
    void testReturnCategory() {
        FactorCategory category = FactorCategory.RETURN;
        assertEquals("RETURN", category.name());
    }

    @Test
    void testRiskCategory() {
        FactorCategory category = FactorCategory.RISK;
        assertEquals("RISK", category.name());
    }

    @Test
    void testScaleCategory() {
        FactorCategory category = FactorCategory.SCALE;
        assertEquals("SCALE", category.name());
    }

    @Test
    void testLiquidityCategory() {
        FactorCategory category = FactorCategory.LIQUIDITY;
        assertEquals("LIQUIDITY", category.name());
    }

    @Test
    void testStyleCategory() {
        FactorCategory category = FactorCategory.STYLE;
        assertEquals("STYLE", category.name());
    }

    @Test
    void testCustomCategory() {
        FactorCategory category = FactorCategory.CUSTOM;
        assertEquals("CUSTOM", category.name());
    }

    @Test
    void testDerivedCategory() {
        FactorCategory category = FactorCategory.DERIVED;
        assertEquals("DERIVED", category.name());
    }

    @Test
    void testValueOfAllCategories() {
        for (FactorCategory category : FactorCategory.values()) {
            assertEquals(category, FactorCategory.valueOf(category.name()));
        }
    }

    @Test
    void testOrdinalOrder() {
        assertEquals(0, FactorCategory.RETURN.ordinal());
        assertEquals(1, FactorCategory.RISK.ordinal());
        assertEquals(2, FactorCategory.SCALE.ordinal());
        assertEquals(3, FactorCategory.LIQUIDITY.ordinal());
        assertEquals(4, FactorCategory.STYLE.ordinal());
        assertEquals(5, FactorCategory.CUSTOM.ordinal());
        assertEquals(6, FactorCategory.DERIVED.ordinal());
    }
}
