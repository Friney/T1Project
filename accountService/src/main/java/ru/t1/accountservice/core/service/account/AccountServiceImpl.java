package ru.t1.accountservice.core.service.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.account.AccountUpdateRequest;
import ru.t1.accountservice.core.annotation.Cached;
import ru.t1.accountservice.core.annotation.LogDataSourceError;
import ru.t1.accountservice.core.annotation.Metric;
import ru.t1.accountservice.core.entity.account.Account;
import ru.t1.accountservice.core.entity.account.AccountStatus;
import ru.t1.accountservice.core.entity.client.Client;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.AccountMapper;
import ru.t1.accountservice.core.repository.AccountRepository;
import ru.t1.accountservice.core.service.client.ClientService;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientService clientService;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAll(long clientId) {
        return accountMapper.map(accountRepository.findAllByClientId(clientId));
    }

    @Override
    @Cached(name = "account")
    @Transactional(readOnly = true)
    public AccountDto getById(long id, long clientId) {
        return accountMapper.map(getEntityById(id, clientId));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public AccountDto create(AccountCreateRequest accountCreateRequest, long clientId) {
        if (!clientService.existsById(clientId)) {
            throw new ServiceException("Client with id " + clientId + " not found", HttpStatus.NOT_FOUND);
        }

        Client client = Client.builder()
                .id(clientId)
                .build();

        Account account = Account.builder()
                .accountId(accountRepository.getNextAccountId())
                .accountType(accountCreateRequest.accountType())
                .balance(accountCreateRequest.balance())
                .frozenAmount(BigDecimal.ZERO)
                .client(client)
                .status(AccountStatus.OPEN)
                .build();

        return accountMapper.map(accountRepository.save(account));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public AccountDto update(AccountUpdateRequest accountUpdateRequest, long id, long clientId) {
        Account account = getEntityById(id, clientId);
        updateAccountFields(account, accountUpdateRequest);
        return accountMapper.map(accountRepository.save(account));
    }

    @Override
    @LogDataSourceError
    @Transactional
    public void delete(long id, long clientId) {
        getEntityById(id, clientId);
        accountRepository.deleteByAccountId(id);
    }

    @Override
    public boolean existsById(long id) {
        return accountRepository.existsByAccountId(id);
    }

    @Transactional(readOnly = true)
    protected Account getEntityById(long id, long clientId) {
        return accountRepository.findByAccountIdAndClientId(id, clientId)
                .orElseThrow(() -> new ServiceException("Account with id " + id + " not found for client with id " + clientId, HttpStatus.NOT_FOUND));
    }

    private void updateAccountFields(Account account, AccountUpdateRequest accountUpdateRequest) {
        Optional.ofNullable(accountUpdateRequest.accountType()).ifPresent(account::setAccountType);
        Optional.ofNullable(accountUpdateRequest.balance()).ifPresent(account::setBalance);
    }
}
