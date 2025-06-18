package ru.t1.accountservice.api.dto.account;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import ru.t1.accountservice.core.entity.account.AccountType;

@Builder
public record AccountCreateRequest(
        @NotNull AccountType accountType,
        @NotNull BigDecimal balance
) {
}
