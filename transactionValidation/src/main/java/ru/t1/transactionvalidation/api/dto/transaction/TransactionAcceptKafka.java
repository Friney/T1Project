package ru.t1.transactionvalidation.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import ru.t1.transactionvalidation.api.dto.account.AccountType;

public record TransactionAcceptKafka(
        Long clientId,
        Long accountId,
        Long transactionId,
        LocalDateTime timestamp,
        BigDecimal transactionAmount,
        BigDecimal accountBalance,
        AccountType accountType
) {
}
