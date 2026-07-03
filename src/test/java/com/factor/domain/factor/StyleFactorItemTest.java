package com.factor.domain.factor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 风格因子组成项领域模型测试
 */
class StyleFactorItemTest {

    @Test
    void testCreateStyleFactorItem() {
        StyleFactorItem item = new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("60.00"));

        assertEquals("sfi-1", item.id());
        assertEquals("sf-1", item.styleFactorId());
        assertEquals("df-1", item.derivativeFactorId());
        assertEquals(new BigDecimal("60.00"), item.weight());
    }

    @Test
    void testStyleFactorItemWithIntegerWeight() {
        StyleFactorItem item = new StyleFactorItem("sfi-2", "sf-1", "df-2", new BigDecimal("40"));

        assertEquals(new BigDecimal("40"), item.weight());
    }

    @Test
    void testStyleFactorItemWithZeroWeight() {
        StyleFactorItem item = new StyleFactorItem("sfi-3", "sf-1", "df-3", BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, item.weight());
    }

    @Test
    void testStyleFactorItemEquality() {
        StyleFactorItem item1 = new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("50"));
        StyleFactorItem item2 = new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("50"));

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void testStyleFactorItemToString() {
        StyleFactorItem item = new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("60"));

        String str = item.toString();
        assertNotNull(str);
        assertTrue(str.contains("sfi-1"));
        assertTrue(str.contains("60"));
    }

    @Test
    void testStyleFactorItemDifferentWeights() {
        StyleFactorItem heavy = new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("80"));
        StyleFactorItem light = new StyleFactorItem("sfi-2", "sf-1", "df-2", new BigDecimal("20"));

        assertEquals(0, heavy.weight().compareTo(new BigDecimal("80")));
        assertEquals(0, light.weight().compareTo(new BigDecimal("20")));
    }
}
