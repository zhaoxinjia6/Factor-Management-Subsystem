package com.factor.domain.factor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子领域模型综合测试
 */
class FactorValueTest {

    @Test
    void testCreateFactorCategory() {
        assertEquals(FactorCategory.RETURN, FactorCategory.RETURN);
        assertEquals(FactorCategory.SCALE, FactorCategory.SCALE);
        assertEquals(FactorCategory.DERIVED, FactorCategory.DERIVED);
    }

    @Test
    void testCreateBaseFactorValue() {
        BaseFactorValue v = new BaseFactorValue("v1", "000001", "bf1", LocalDate.of(2024, 1, 5), BigDecimal.valueOf(0.5), LocalDateTime.now());
        assertEquals("v1", v.id());
        assertEquals("000001", v.fundCode());
        assertEquals(0.5, v.value().doubleValue(), 0.001);
    }

    @Test
    void testCreateDerivativeFactorItem() {
        DerivativeFactorItem item = new DerivativeFactorItem("i1", "df1", "bf1", BigDecimal.valueOf(30));
        assertEquals("i1", item.id());
        assertEquals(30, item.weight().doubleValue(), 0.001);
    }

    @Test
    void testCreateDerivativeFactorValue() {
        DerivativeFactorValue v = new DerivativeFactorValue("v1", "000001", "df1", LocalDate.of(2024, 1, 5), BigDecimal.valueOf(0.85), LocalDateTime.now());
        assertEquals("df1", v.derivativeFactorId());
        assertEquals(0.85, v.value().doubleValue(), 0.001);
    }

    @Test
    void testCreateStyleFactorItem() {
        StyleFactorItem item = new StyleFactorItem("si1", "sf1", "df1", BigDecimal.valueOf(50));
        assertEquals("sf1", item.styleFactorId());
        assertEquals(50, item.weight().doubleValue(), 0.001);
    }

    @Test
    void testCreateStyleFactorValue() {
        StyleFactorValue v = new StyleFactorValue("sv1", "000001", "sf1", LocalDate.of(2024, 1, 5), BigDecimal.valueOf(1.25), LocalDateTime.now());
        assertEquals("sf1", v.styleFactorId());
        assertEquals(1.25, v.value().doubleValue(), 0.001);
    }

    @Test
    void testCreateFactor() {
        FactorCategory cat = FactorCategory.RETURN;
        Factor f = new Factor("f1", "PE", "市盈率", cat, "manual", "市盈率因子", BigDecimal.valueOf(15.5), "倍", Map.of(), LocalDateTime.now(), null);
        assertEquals("f1", f.id());
        assertEquals("PE", f.code());
        assertEquals("市盈率", f.name());
        assertEquals("manual", f.source());
    }

    @Test
    void testCreateFactorTreeNode() {
        FactorTreeNode node = new FactorTreeNode("n1", "f1", null, "市盈率", 1, List.of());
        assertEquals("n1", node.id());
        assertEquals("市盈率", node.displayName());
        assertTrue(node.children().isEmpty());
    }

    @Test
    void testCreateFactorTreeNodeWithChildren() {
        FactorTreeNode child = new FactorTreeNode("n2", "f2", "n1", "管理费率", 2, List.of());
        FactorTreeNode parent = new FactorTreeNode("n1", "f1", null, "费率类", 1, List.of(child));
        assertEquals(1, parent.children().size());
        assertEquals("管理费率", parent.children().get(0).displayName());
    }

    @Test
    void testCreateFactorCategoryNode() {
        FactorCategoryNode node = new FactorCategoryNode("cn1", null, "费率水平", 0, 1, "费率类因子", true, List.of());
        assertEquals("cn1", node.id());
        assertEquals("费率水平", node.name());
        assertTrue(node.children().isEmpty());
    }

    @Test
    void testCreateFactorCategoryNodeWithChildren() {
        FactorCategoryNode child = new FactorCategoryNode("cn2", "cn1", "管理费率", 1, 2, "管理费率因子", true, List.of());
        FactorCategoryNode parent = new FactorCategoryNode("cn1", null, "费率类", 0, 1, "费率相关", true, List.of(child));
        assertEquals(1, parent.children().size());
        assertEquals("cn2", parent.children().get(0).id());
    }

    @Test
    void testCreateFactorTree() {
        FactorTreeNode node = new FactorTreeNode("n1", "f1", null, "市盈率", 1, List.of());
        FactorTree tree = new FactorTree("t1", "估值因子树", "估值分析场景", List.of(node), LocalDateTime.now(), null);
        assertEquals("t1", tree.id());
        assertEquals(1, tree.nodes().size());
    }

    @Test
    void testCreateFormula() {
        Formula formula = new Formula("fml1", "日收益率", "(close/shift(close,1)-1)*100",
                List.of(new FormulaItem("close", "收盘价", null, null)),
                LocalDateTime.now(), null);
        assertEquals("fml1", formula.id());
        assertEquals("日收益率", formula.name());
    }

    @Test
    void testCreateFormulaItem() {
        FormulaItem item = new FormulaItem("close", "收盘价", BigDecimal.valueOf(1), "+");
        assertEquals("close", item.factorId());
        assertEquals("收盘价", item.factorName());
    }

    @Test
    void testCreateDerivedFactor() {
        DerivedFactor df = new DerivedFactor("d1", "5日动量", "fml1", BigDecimal.valueOf(0.05), "%", "active",
                LocalDateTime.now(), null);
        assertEquals("d1", df.id());
        assertEquals("5日动量", df.name());
    }

    @Test
    void testFactorCategoryNodeEnabled() {
        FactorCategoryNode enabled = new FactorCategoryNode("c1", null, "A", 0, 1, "", true, List.of());
        FactorCategoryNode disabled = new FactorCategoryNode("c2", null, "B", 0, 2, "", false, List.of());
        assertTrue(enabled.enabled());
        assertFalse(disabled.enabled());
    }

    @Test
    void testDerivativeFactorValueCalculatedAt() {
        LocalDateTime now = LocalDateTime.now();
        DerivativeFactorValue v = new DerivativeFactorValue("v1", "000001", "df1", LocalDate.now(), BigDecimal.ONE, now);
        assertEquals(now, v.calculatedAt());
    }

    @Test
    void testBaseFactorValueUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        BaseFactorValue v = new BaseFactorValue("v1", "000001", "bf1", LocalDate.now(), BigDecimal.TEN, now);
        assertEquals(now, v.updatedAt());
    }
}
