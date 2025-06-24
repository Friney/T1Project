package ru.t1.accountservice.api.dto.account;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.core.entity.account.AccountStatus;
import ru.t1.accountservice.core.entity.account.AccountType;

@Builder
public record AccountDto(
        Long id,
        AccountType accountType,
        BigDecimal balance,
        BigDecimal frozenAmount,
        List<TransactionDto> transactions,
        AccountStatus status
) {
}
