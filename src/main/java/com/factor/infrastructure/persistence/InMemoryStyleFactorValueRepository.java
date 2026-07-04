package com.factor.infrastructure.persistence;

import com.factor.domain.factor.StyleFactorValue;
import com.factor.domain.factor.repository.StyleFactorValueRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
@Profile("!jpa")
@Repository
public class InMemoryStyleFactorValueRepository implements StyleFactorValueRepository {

    private final List<StyleFactorValue> storage = List.of(
            new StyleFactorValue("sfv-1", "000001", "sf-1", LocalDate.now().minusDays(2), new BigDecimal("0.1221"), LocalDateTime.now()),
            new StyleFactorValue("sfv-2", "000001", "sf-1", LocalDate.now().minusDays(1), new BigDecimal("0.1240"), LocalDateTime.now()),
            new StyleFactorValue("sfv-3", "000001", "sf-1", LocalDate.now(), new BigDecimal("0.1256"), LocalDateTime.now())
    );

    @Override
    public List<StyleFactorValue> query(String fundCode, String factorId) {
        return storage.stream()
                .filter(item -> fundCode == null || fundCode.isEmpty() || item.fundCode().equals(fundCode))
                .filter(item -> factorId == null || factorId.isEmpty() || item.styleFactorId().equals(factorId))
                .toList();
    }
}
