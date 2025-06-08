package ru.t1.accountservice.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import ru.t1.accountservice.core.entity.account.AccountType;

@Builder
public record TransactionAccept(
        Long clientId,
        Long accountId,
        Long transactionId,
        LocalDateTime timestamp,
        BigDecimal transactionAmount,
        BigDecimal accountBalance,
        AccountType accountType
) {
}
