package ru.t1.accountservice.core.service.account;

import java.util.List;
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.account.AccountUpdateRequest;

public interface AccountService {

    List<AccountDto> getAll(long clientId);

    AccountDto getById(long id, long clientId);

    AccountDto create(AccountCreateRequest accountCreateRequest, long clientId);

    AccountDto update(AccountUpdateRequest accountUpdateRequest, long id, long clientId);

    void delete(long id, long clientId);
}
