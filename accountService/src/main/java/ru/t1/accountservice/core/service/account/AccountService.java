package ru.t1.accountservice.core.service.account;

import java.math.BigDecimal;
import java.util.List;
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.account.AccountUpdateRequest;
import ru.t1.accountservice.core.entity.account.AccountStatus;

public interface AccountService {

    List<AccountDto> getAll(long clientId);

    AccountDto getById(long id, long clientId);

    AccountDto getOnlyById(long id);

    AccountDto create(AccountCreateRequest accountCreateRequest, long clientId);

    AccountDto update(AccountUpdateRequest accountUpdateRequest, long id, long clientId);

    void addAmount(long id, BigDecimal amount);

    void updateAccountForBlockedTransaction(long id, BigDecimal amount, BigDecimal frozenAmount, AccountStatus status);

    Long getClientIdByAccountId(long id);

    void delete(long id, long clientId);

    boolean existsById(long id);
}
