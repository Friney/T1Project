package ru.t1.accountservice.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TransactionDto(
        Long id,
        BigDecimal amount,
        LocalDateTime transactionTime
) {
}
