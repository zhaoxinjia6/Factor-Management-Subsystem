package com.factor.infrastructure.persistence;

import com.factor.domain.factor.BaseFactorValue;
import com.factor.domain.factor.repository.BaseFactorValueRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
@Profile("!jpa")
@Repository
public class InMemoryBaseFactorValueRepository implements BaseFactorValueRepository {

    private final List<BaseFactorValue> storage = List.of(
            new BaseFactorValue("bfv-1", "000001", "bf-1", LocalDate.now().minusDays(2), new BigDecimal("0.1234"), LocalDateTime.now()),
            new BaseFactorValue("bfv-2", "000001", "bf-1", LocalDate.now().minusDays(1), new BigDecimal("0.1245"), LocalDateTime.now()),
            new BaseFactorValue("bfv-3", "000001", "bf-1", LocalDate.now(), new BigDecimal("0.1251"), LocalDateTime.now())
    );

    @Override
    public List<BaseFactorValue> query(String fundCode, String factorId) {
        return storage.stream()
                .filter(item -> fundCode == null || fundCode.isEmpty() || item.fundCode().equals(fundCode))
                .filter(item -> factorId == null || factorId.isEmpty() || item.baseFactorId().equals(factorId))
                .toList();
    }
}
