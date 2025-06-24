package ru.t1.accountservice.api.dto.client;

import java.util.List;
import lombok.Builder;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.core.entity.client.ClientStatus;

@Builder
public record ClientDto(
        Long id,
        String firstName,
        String middleName,
        String lastName,
        ClientStatus status,
        List<AccountDto> accounts
) {
}
