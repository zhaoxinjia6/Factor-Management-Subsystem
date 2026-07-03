package com.factor.infrastructure.persistence;

import com.factor.domain.factor.StyleFactorDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 风格因子仓储内存实现测试
 */
class InMemoryStyleFactorRepositoryTest {

    private InMemoryStyleFactorRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryStyleFactorRepository();
    }

    @Test
    void testFindAll() {
        List<StyleFactorDefinition> factors = repository.findAll();
        assertNotNull(factors);
        assertTrue(factors.size() > 0);
    }

    @Test
    void testFindByIdExisting() {
        Optional<StyleFactorDefinition> factor = repository.findById("sf-1");
        assertTrue(factor.isPresent());
        assertEquals("稳健风格因子", factor.get().name());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<StyleFactorDefinition> factor = repository.findById("non-existent");
        assertFalse(factor.isPresent());
    }

    @Test
    void testSaveNewStyleFactor() {
        StyleFactorDefinition newFactor = new StyleFactorDefinition(
                null, "成长风格因子", "system", java.time.LocalDateTime.now(),
                "成长型投资风格", true);

        StyleFactorDefinition saved = repository.save(newFactor);
        assertNotNull(saved.id());
        assertEquals("成长风格因子", saved.name());
    }

    @Test
    void testSaveExistingStyleFactor() {
        StyleFactorDefinition original = repository.findById("sf-1").orElseThrow();
        StyleFactorDefinition updated = repository.save(new StyleFactorDefinition(
                "sf-1", original.name() + "更新", original.createdBy(),
                original.createdAt(), original.description(), original.enabled()));

        Optional<StyleFactorDefinition> found = repository.findById("sf-1");
        assertTrue(found.isPresent());
        assertTrue(found.get().name().contains("更新"));
    }

    @Test
    void testFindAllReturnsImmutable() {
        List<StyleFactorDefinition> factors = repository.findAll();
        assertThrows(UnsupportedOperationException.class, () -> factors.add(null));
    }

    @Test
    void testSaveMultipleFactors() {
        StyleFactorDefinition f1 = repository.save(new StyleFactorDefinition(
                null, "因子1", "admin", java.time.LocalDateTime.now(), "描述1", true));
        StyleFactorDefinition f2 = repository.save(new StyleFactorDefinition(
                null, "因子2", "admin", java.time.LocalDateTime.now(), "描述2", false));

        assertNotNull(f1.id());
        assertNotNull(f2.id());
        assertNotEquals(f1.id(), f2.id());
    }

    @Test
    void testSaveWithDisabledState() {
        StyleFactorDefinition disabled = repository.save(new StyleFactorDefinition(
                null, "禁用因子", "system", java.time.LocalDateTime.now(), "禁用", false));
        assertFalse(disabled.enabled());
    }
}
