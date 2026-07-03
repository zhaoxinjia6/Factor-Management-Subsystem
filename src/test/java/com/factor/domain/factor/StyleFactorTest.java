package com.factor.domain.factor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 风格因子领域模型测试
 */
class StyleFactorTest {

    @Test
    void testCreateStyleFactor() {
        LocalDateTime now = LocalDateTime.now();
        StyleFactor styleFactor = new StyleFactor("sf-1", "稳健风格因子", "收益与风险",
                "稳健收益风格", now, now);

        assertEquals("sf-1", styleFactor.id());
        assertEquals("稳健风格因子", styleFactor.name());
        assertEquals("收益与风险", styleFactor.category());
        assertEquals("稳健收益风格", styleFactor.description());
        assertEquals(now, styleFactor.createdAt());
        assertEquals(now, styleFactor.updatedAt());
    }

    @Test
    void testStyleFactorWithEmptyCategory() {
        LocalDateTime now = LocalDateTime.now();
        StyleFactor styleFactor = new StyleFactor("sf-2", "成长风格", "",
                "成长型投资风格", now, now);

        assertEquals("", styleFactor.category());
    }

    @Test
    void testStyleFactorWithNullDescription() {
        LocalDateTime now = LocalDateTime.now();
        StyleFactor styleFactor = new StyleFactor("sf-3", "价值风格", "估值", null, now, now);

        assertNull(styleFactor.description());
    }

    @Test
    void testStyleFactorEquality() {
        LocalDateTime now = LocalDateTime.now();
        StyleFactor sf1 = new StyleFactor("sf-1", "稳健风格", "收益", "描述", now, now);
        StyleFactor sf2 = new StyleFactor("sf-1", "稳健风格", "收益", "描述", now, now);

        assertEquals(sf1, sf2);
        assertEquals(sf1.hashCode(), sf2.hashCode());
    }

    @Test
    void testStyleFactorToString() {
        LocalDateTime now = LocalDateTime.now();
        StyleFactor sf = new StyleFactor("sf-1", "稳健风格因子", "收益与风险",
                "稳健收益风格", now, now);

        String str = sf.toString();
        assertNotNull(str);
        assertTrue(str.contains("sf-1"));
        assertTrue(str.contains("稳健风格因子"));
    }

    @Test
    void testStyleFactorDifferentUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        StyleFactor sf1 = new StyleFactor("sf-1", "因子1", "cat1", "desc1", now, now);
        StyleFactor sf2 = new StyleFactor("sf-1", "因子1", "cat1", "desc1", now, now.plusDays(1));

        assertEquals(sf1.createdAt(), sf2.createdAt());
        assertNotEquals(sf1.updatedAt(), sf2.updatedAt());
    }
}
