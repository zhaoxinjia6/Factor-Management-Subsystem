package com.factor.infrastructure.persistence.jpa;

import com.factor.domain.factor.FactorCategoryNode;
import com.factor.domain.factor.repository.FactorCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaFactorCategoryRepositoryTest {

    @Mock
    private FactorCategoryJpaRepository factorCategoryJpaRepository;

    @InjectMocks
    private JpaFactorCategoryRepository jpaFactorCategoryRepository;

    private FactorCategoryEntity rootEntity;
    private FactorCategoryEntity childEntity;

    @BeforeEach
    void setUp() {
        rootEntity = new FactorCategoryEntity();
        rootEntity.setId("c1");
        rootEntity.setParentId(null);
        rootEntity.setName("收益类");
        rootEntity.setCatLevel(1);
        rootEntity.setSortNo(1);
        rootEntity.setEnabled(true);

        childEntity = new FactorCategoryEntity();
        childEntity.setId("c2");
        childEntity.setParentId("c1");
        childEntity.setName("股票收益");
        childEntity.setCatLevel(2);
        childEntity.setSortNo(1);
        childEntity.setEnabled(true);
    }

    // ==================== findTree 测试 ====================

    @Test
    void findTree_shouldReturnTreeWithRootAndChildren() {
        when(factorCategoryJpaRepository.findAll()).thenReturn(List.of(rootEntity, childEntity));

        List<FactorCategoryNode> result = jpaFactorCategoryRepository.findTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("收益类", result.get(0).name());
        assertEquals(1, result.get(0).children().size());
        assertEquals("股票收益", result.get(0).children().get(0).name());
    }

    @Test
    void findTree_shouldReturnEmptyList_whenNoCategories() {
        when(factorCategoryJpaRepository.findAll()).thenReturn(List.of());

        List<FactorCategoryNode> result = jpaFactorCategoryRepository.findTree();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findTree_shouldHandleMultipleRoots() {
        FactorCategoryEntity root2 = new FactorCategoryEntity();
        root2.setId("c3");
        root2.setParentId(null);
        root2.setName("风险类");
        root2.setCatLevel(1);
        root2.setSortNo(2);
        root2.setEnabled(true);

        when(factorCategoryJpaRepository.findAll()).thenReturn(List.of(rootEntity, root2));

        List<FactorCategoryNode> result = jpaFactorCategoryRepository.findTree();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== findById 测试 ====================

    @Test
    void findById_shouldReturnCategory_whenExists() {
        when(factorCategoryJpaRepository.findById("c1")).thenReturn(Optional.of(rootEntity));
        when(factorCategoryJpaRepository.findByParentId("c1")).thenReturn(List.of(childEntity));

        Optional<FactorCategoryNode> result = jpaFactorCategoryRepository.findById("c1");

        assertTrue(result.isPresent());
        assertEquals("收益类", result.get().name());
        assertEquals(1, result.get().children().size());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(factorCategoryJpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<FactorCategoryNode> result = jpaFactorCategoryRepository.findById("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ==================== save 测试 ====================

    @Test
    void save_shouldCreateNew_whenIdIsNull() {
        FactorCategoryNode newNode = new FactorCategoryNode(null, null, "新分类", 1, 1, "描述", true, List.of());

        when(factorCategoryJpaRepository.save(any(FactorCategoryEntity.class)))
                .thenReturn(rootEntity);

        FactorCategoryNode result = jpaFactorCategoryRepository.save(newNode);

        assertNotNull(result);
        verify(factorCategoryJpaRepository, times(1)).save(any(FactorCategoryEntity.class));
    }

    @Test
    void save_shouldUpdateExisting_whenIdIsNotNull() {
        FactorCategoryNode existingNode = new FactorCategoryNode("c1", null, "更新分类", 1, 1, "描述", true, List.of());

        when(factorCategoryJpaRepository.save(any(FactorCategoryEntity.class)))
                .thenReturn(rootEntity);

        FactorCategoryNode result = jpaFactorCategoryRepository.save(existingNode);

        assertNotNull(result);
        verify(factorCategoryJpaRepository, times(1)).save(any(FactorCategoryEntity.class));
    }
}