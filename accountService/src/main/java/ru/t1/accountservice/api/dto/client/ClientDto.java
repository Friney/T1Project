package ru.t1.accountservice.api.dto.client;

import java.util.List;
import lombok.Builder;
import ru.t1.accountservice.api.dto.account.AccountDto;

@Builder
public record ClientDto(
        Long id,
        String firstName,
        String middleName,
        String lastName,
        List<AccountDto> accounts
) {
}
