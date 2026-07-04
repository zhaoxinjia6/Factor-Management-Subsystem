package com.factor.infrastructure.persistence;

import com.factor.domain.factor.DerivativeFactorValue;
import com.factor.domain.factor.repository.DerivativeFactorValueRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
@Profile("!jpa")
@Repository
public class InMemoryDerivativeFactorValueRepository implements DerivativeFactorValueRepository {

    private final List<DerivativeFactorValue> storage = List.of(
            new DerivativeFactorValue("dfv-1", "000001", "df-1", LocalDate.now().minusDays(2), new BigDecimal("0.1122"), LocalDateTime.now()),
            new DerivativeFactorValue("dfv-2", "000001", "df-1", LocalDate.now().minusDays(1), new BigDecimal("0.1138"), LocalDateTime.now()),
            new DerivativeFactorValue("dfv-3", "000001", "df-1", LocalDate.now(), new BigDecimal("0.1150"), LocalDateTime.now())
    );

    @Override
    public List<DerivativeFactorValue> query(String fundCode, String factorId) {
        return storage.stream()
                .filter(item -> fundCode == null || fundCode.isEmpty() || item.fundCode().equals(fundCode))
                .filter(item -> factorId == null || factorId.isEmpty() || item.derivativeFactorId().equals(factorId))
                .toList();
    }
}
