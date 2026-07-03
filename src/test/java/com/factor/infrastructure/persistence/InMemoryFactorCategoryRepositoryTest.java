package com.factor.infrastructure.persistence;

import com.factor.domain.factor.FactorCategoryNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子分类仓储内存实现测试
 */
class InMemoryFactorCategoryRepositoryTest {

    private InMemoryFactorCategoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFactorCategoryRepository();
    }

    @Test
    void testFindTree() {
        List<FactorCategoryNode> tree = repository.findTree();
        assertNotNull(tree);
        assertTrue(tree.size() > 0);
    }

    @Test
    void testFindTreeHasRootNodes() {
        List<FactorCategoryNode> tree = repository.findTree();
        assertTrue(tree.stream().anyMatch(n -> n.parentId() == null));
    }

    @Test
    void testFindTreeHasChildren() {
        List<FactorCategoryNode> tree = repository.findTree();
        assertTrue(tree.stream().anyMatch(n -> n.children() != null && !n.children().isEmpty()));
    }

    @Test
    void testFindByIdExisting() {
        Optional<FactorCategoryNode> node = repository.findById("cat-1");
        assertTrue(node.isPresent());
        assertEquals("费率水平", node.get().name());
    }

    @Test
    void testFindByIdChildNode() {
        Optional<FactorCategoryNode> node = repository.findById("cat-1-1");
        assertTrue(node.isPresent());
        assertEquals("管理费率", node.get().name());
        assertEquals(2, node.get().level());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<FactorCategoryNode> node = repository.findById("non-existent");
        assertFalse(node.isPresent());
    }

    @Test
    void testSaveNewCategory() {
        FactorCategoryNode newCategory = new FactorCategoryNode(
                null, "cat-1", "新子分类", 2, 4, "新分类", true, List.of());

        FactorCategoryNode saved = repository.save(newCategory);
        assertNotNull(saved.id());
        assertEquals("新子分类", saved.name());
    }

    @Test
    void testSaveExistingCategory() {
        FactorCategoryNode updated = repository.save(new FactorCategoryNode(
                "cat-1", null, "费率水平更新", 1, 1, "费率更新", true, List.of()));

        Optional<FactorCategoryNode> found = repository.findById("cat-1");
        assertTrue(found.isPresent());
        assertEquals("费率水平更新", found.get().name());
    }

    @Test
    void testFindTreeReturnsImmutable() {
        List<FactorCategoryNode> tree = repository.findTree();
        assertThrows(UnsupportedOperationException.class, () -> tree.add(null));
    }

    @Test
    void testTreeStructure() {
        List<FactorCategoryNode> tree = repository.findTree();
        FactorCategoryNode cat1 = tree.stream().filter(n -> n.id().equals("cat-1")).findFirst().orElseThrow();
        assertEquals(3, cat1.children().size());
    }

    @Test
    void testSecondRootCategory() {
        List<FactorCategoryNode> tree = repository.findTree();
        FactorCategoryNode cat2 = tree.stream().filter(n -> n.id().equals("cat-2")).findFirst().orElseThrow();
        assertEquals("规模与仓位", cat2.name());
        assertEquals(3, cat2.children().size());
    }
}
