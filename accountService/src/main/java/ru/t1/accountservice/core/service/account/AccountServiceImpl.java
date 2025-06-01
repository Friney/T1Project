package ru.t1.accountservice.core.service.account;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.account.AccountUpdateRequest;
import ru.t1.accountservice.core.entity.account.Account;
import ru.t1.accountservice.core.entity.client.Client;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.AccountMapper;
import ru.t1.accountservice.core.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAll(long clientId) {
        return accountMapper.map(accountRepository.findAllByClientId(clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getById(long id, long clientId) {
        return accountMapper.map(getEntityById(clientId, id));
    }

    @Transactional(readOnly = true)
    protected Account getEntityById(long id, long clientId) {
        return accountRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ServiceException("Account with id " + id + " not found for client with id " + clientId, HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public AccountDto create(AccountCreateRequest accountCreateRequest, long clientId) {
        Client client = Client.builder()
                .id(clientId)
                .build();

        Account account = Account.builder()
                .accountType(accountCreateRequest.accountType())
                .balance(accountCreateRequest.balance())
                .client(client)
                .build();

        return accountMapper.map(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto update(AccountUpdateRequest accountUpdateRequest, long id, long clientId) {
        Account account = getEntityById(clientId, id);
        updateAccountFields(account, accountUpdateRequest);
        return accountMapper.map(accountRepository.save(account));
    }

    private void updateAccountFields(Account account, AccountUpdateRequest accountUpdateRequest) {
        Optional.ofNullable(accountUpdateRequest.accountType()).ifPresent(account::setAccountType);
        Optional.ofNullable(accountUpdateRequest.balance()).ifPresent(account::setBalance);
    }

    @Override
    @Transactional
    public void delete(long id, long clientId) {
        getEntityById(clientId, id);
        accountRepository.deleteById(id);
    }
}
