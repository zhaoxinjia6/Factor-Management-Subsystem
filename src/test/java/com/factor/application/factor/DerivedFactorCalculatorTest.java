package com.factor.application.factor;

import com.factor.domain.factor.Formula;
import com.factor.domain.factor.FormulaItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DerivedFactorCalculatorTest {

    private DerivedFactorCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DerivedFactorCalculator();
    }

    // ========== 基础运算测试 ==========

    @Test
    void testCalculateAddition() {
        Formula formula = new Formula(
                "formula-001",
                "加法测试",
                "f1 + f2",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "+")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("10"),
                "f2", new BigDecimal("20")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("30"), result);
    }

    @Test
    void testCalculateSubtraction() {
        Formula formula = new Formula(
                "formula-002",
                "减法测试",
                "f1 - f2",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "-")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("50"),
                "f2", new BigDecimal("20")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("30"), result);
    }

    @Test
    void testCalculateMultiplication() {
        Formula formula = new Formula(
                "formula-003",
                "乘法测试",
                "f1 * f2",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "*")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("5"),
                "f2", new BigDecimal("10")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("50"), result);
    }

    @Test
    void testCalculateDivision() {
        Formula formula = new Formula(
                "formula-004",
                "除法测试",
                "f1 / f2",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "/")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("100"),
                "f2", new BigDecimal("4")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("25"), result);
    }

    @Test
    void testCalculateDivisionByZero() {
        Formula formula = new Formula(
                "formula-005",
                "除零测试",
                "f1 / f2",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "/")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("100"),
                "f2", BigDecimal.ZERO
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    void testCalculateMax() {
        Formula formula = new Formula(
                "formula-006",
                "最大值测试",
                "MAX(f1, f2)",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "MAX")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("10"),
                "f2", new BigDecimal("20")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("20"), result);
    }

    @Test
    void testCalculateMin() {
        Formula formula = new Formula(
                "formula-007",
                "最小值测试",
                "MIN(f1, f2)",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "MIN")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("10"),
                "f2", new BigDecimal("20")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("10"), result);
    }

    @Test
    void testCalculateAvg() {
        Formula formula = new Formula(
                "formula-008",
                "平均值测试",
                "AVG(f1, f2)",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "AVG")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("10"),
                "f2", new BigDecimal("20")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("15"), result);
    }

    @Test
    void testCalculateWithWeight() {
        Formula formula = new Formula(
                "formula-009",
                "加权测试",
                "f1 * 0.3 + f2 * 0.7",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("0.3"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("0.7"), "+")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("100"),
                "f2", new BigDecimal("200")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("170"), result);
    }

    @Test
    void testCalculateWithNullWeight() {
        Formula formula = new Formula(
                "formula-010",
                "空权重测试",
                "f1 + f2",
                List.of(
                        new FormulaItem("f1", "因子1", null, "+"),
                        new FormulaItem("f2", "因子2", null, "+")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("10"),
                "f2", new BigDecimal("20")
        );

        BigDecimal result = calculator.calculate(formula, factorValues);
        assertEquals(new BigDecimal("30"), result);
    }

    // ========== 异常测试 ==========

    @Test
    void testCalculateWithNullFormula() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(null, Map.of());
        });
    }

    @Test
    void testCalculateWithNullFormulaItems() {
        Formula formula = new Formula(
                "formula-011",
                "空项测试",
                "",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(formula, Map.of());
        });
    }

    @Test
    void testCalculateWithEmptyFormulaItems() {
        Formula formula = new Formula(
                "formula-012",
                "空项测试",
                "",
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(formula, Map.of());
        });
    }

    @Test
    void testCalculateWithMissingFactorValue() {
        Formula formula = new Formula(
                "formula-013",
                "缺失值测试",
                "f1",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f2", new BigDecimal("20")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(formula, factorValues);
        });
    }

    @Test
    void testCalculateWithUnsupportedOperator() {
        Formula formula = new Formula(
                "formula-014",
                "非法操作符测试",
                "f1 INVALID f2",
                List.of(
                        new FormulaItem("f1", "因子1", new BigDecimal("1.0"), "+"),
                        new FormulaItem("f2", "因子2", new BigDecimal("1.0"), "INVALID")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Map<String, BigDecimal> factorValues = Map.of(
                "f1", new BigDecimal("10"),
                "f2", new BigDecimal("20")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(formula, factorValues);
        });
    }
}
// 测试文件
// test contribution 2026-07-04

// test contribution from zhaoxinjia 2026-07-04
