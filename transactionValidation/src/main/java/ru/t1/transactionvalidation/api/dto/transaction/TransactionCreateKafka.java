package ru.t1.transactionvalidation.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TransactionCreateKafka(
        BigDecimal amount,
        LocalDateTime transactionTime,
        Long accountId
) {
}
