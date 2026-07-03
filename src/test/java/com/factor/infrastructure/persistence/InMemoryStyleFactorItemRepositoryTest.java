package com.factor.infrastructure.persistence;

import com.factor.domain.factor.StyleFactorItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 风格因子组成项仓储内存实现测试
 */
class InMemoryStyleFactorItemRepositoryTest {

    private InMemoryStyleFactorItemRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryStyleFactorItemRepository();
    }

    @Test
    void testFindByStyleFactorIdEmpty() {
        List<StyleFactorItem> items = repository.findByStyleFactorId("sf-1");
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testSaveAll() {
        List<StyleFactorItem> newItems = List.of(
                new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("60")),
                new StyleFactorItem("sfi-2", "sf-1", "df-2", new BigDecimal("40"))
        );

        List<StyleFactorItem> saved = repository.saveAll(newItems);
        assertEquals(2, saved.size());
    }

    @Test
    void testFindByStyleFactorIdAfterSave() {
        repository.saveAll(List.of(
                new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("60")),
                new StyleFactorItem("sfi-2", "sf-1", "df-2", new BigDecimal("40"))
        ));

        List<StyleFactorItem> items = repository.findByStyleFactorId("sf-1");
        assertEquals(2, items.size());
    }

    @Test
    void testFindByDifferentStyleFactorId() {
        repository.saveAll(List.of(
                new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("60")),
                new StyleFactorItem("sfi-2", "sf-2", "df-2", new BigDecimal("100"))
        ));

        List<StyleFactorItem> items = repository.findByStyleFactorId("sf-1");
        assertEquals(1, items.size());
        assertEquals("sf-1", items.get(0).styleFactorId());
    }

    @Test
    void testSaveAllReturnsCopy() {
        List<StyleFactorItem> newItems = List.of(
                new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("100"))
        );

        List<StyleFactorItem> saved = repository.saveAll(newItems);
        assertEquals(1, saved.size());
    }

    @Test
    void testMultipleSaveAllCalls() {
        repository.saveAll(List.of(
                new StyleFactorItem("sfi-1", "sf-1", "df-1", new BigDecimal("50"))
        ));
        repository.saveAll(List.of(
                new StyleFactorItem("sfi-2", "sf-1", "df-2", new BigDecimal("50"))
        ));

        List<StyleFactorItem> items = repository.findByStyleFactorId("sf-1");
        assertEquals(2, items.size());
    }
}
