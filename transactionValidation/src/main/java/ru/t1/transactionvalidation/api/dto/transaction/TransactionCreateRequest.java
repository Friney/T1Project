package ru.t1.transactionvalidation.api.dto.transaction;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreateRequest(
        @NotNull BigDecimal amount,
        @NotNull LocalDateTime transactionTime
) {
}
