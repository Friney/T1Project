package ru.t1.accountservice.api.dto.account;

import java.math.BigDecimal;
import ru.t1.accountservice.core.entity.account.AccountType;

public record AccountUpdateRequest(
        AccountType accountType,
        BigDecimal balance
) {
}
