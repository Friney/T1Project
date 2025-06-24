package ru.t1.accountservice.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionUpdateRequest(
        BigDecimal amount,
        LocalDateTime transactionTime
) {
}
