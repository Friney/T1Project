package ru.t1.transactionvalidation.api.dto.transaction;

import lombok.Builder;

@Builder
public record TransactionResultKafka(
        Long accountId,
        Long transactionId,
        TransactionResultStatus transactionResultStatus
) {
}
