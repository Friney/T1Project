package ru.t1.accountservice.core.service.account;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.account.AccountUpdateRequest;
import ru.t1.accountservice.core.entity.account.Account;
import ru.t1.accountservice.core.entity.account.AccountStatus;
import ru.t1.accountservice.core.entity.account.AccountType;
import ru.t1.accountservice.core.entity.client.Client;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.AccountMapper;
import ru.t1.accountservice.core.repository.AccountRepository;
import ru.t1.accountservice.core.service.client.ClientService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private ClientService clientService;
    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private AccountDto testAccountDto;
    private Client testClient;
    private final long testAccountId = 1L;
    private final long testClientId = 1L;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .clientId(testClientId)
                .build();

        testAccount = Account.builder()
                .id(1L)
                .accountId(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(1000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .client(testClient)
                .build();

        testAccountDto = AccountDto.builder()
                .id(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(1000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .build();
    }

    @Test
    void getAllSuccess() {
        List<Account> accounts = Collections.singletonList(testAccount);
        List<AccountDto> expectedDtos = Collections.singletonList(testAccountDto);

        when(accountRepository.findAllByClientId(testClientId)).thenReturn(accounts);
        when(accountMapper.map(accounts)).thenReturn(expectedDtos);

        List<AccountDto> result = accountService.getAll(testClientId);

        assertEquals(expectedDtos, result);
        verify(accountRepository).findAllByClientId(testClientId);
        verify(accountMapper).map(accounts);
    }

    @Test
    void getAllEmptyList() {
        when(accountRepository.findAllByClientId(testClientId)).thenReturn(Collections.emptyList());
        when(accountMapper.map(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<AccountDto> result = accountService.getAll(testClientId);

        assertTrue(result.isEmpty());
        verify(accountRepository).findAllByClientId(testClientId);
        verify(accountMapper).map(Collections.emptyList());
    }

    @Test
    void getByIdSuccess() {
        when(accountRepository.findByAccountIdAndClientId(testAccountId, testClientId))
                .thenReturn(Optional.of(testAccount));
        when(accountMapper.map(testAccount)).thenReturn(testAccountDto);

        AccountDto result = accountService.getById(testAccountId, testClientId);

        assertEquals(testAccountDto, result);
        verify(accountRepository).findByAccountIdAndClientId(testAccountId, testClientId);
        verify(accountMapper).map(testAccount);
    }

    @Test
    void getByIdNotFound() {
        when(accountRepository.findByAccountIdAndClientId(testAccountId, testClientId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.getById(testAccountId, testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountIdAndClientId(testAccountId, testClientId);
    }

    @Test
    void getOnlyByIdSuccess() {
        when(accountRepository.findByAccountId(testAccountId))
                .thenReturn(Optional.of(testAccount));
        when(accountMapper.map(testAccount)).thenReturn(testAccountDto);

        AccountDto result = accountService.getOnlyById(testAccountId);

        assertEquals(testAccountDto, result);
        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountMapper).map(testAccount);
    }

    @Test
    void getOnlyByIdNotFound() {
        when(accountRepository.findByAccountId(testAccountId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.getOnlyById(testAccountId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountId(testAccountId);
    }

    @Test
    void createSuccess() {
        AccountCreateRequest createRequest = new AccountCreateRequest(
                AccountType.DEBIT,
                BigDecimal.valueOf(1000)
        );

        when(clientService.existsById(testClientId)).thenReturn(true);
        when(accountRepository.getNextAccountId()).thenReturn(testAccountId);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.map(testAccount)).thenReturn(testAccountDto);

        AccountDto result = accountService.create(createRequest, testClientId);

        assertEquals(testAccountDto, result);
        verify(clientService).existsById(testClientId);
        verify(accountRepository).getNextAccountId();
        verify(accountRepository).save(any(Account.class));
        verify(accountMapper).map(testAccount);
    }

    @Test
    void createClientNotFound() {
        AccountCreateRequest createRequest = new AccountCreateRequest(
                AccountType.DEBIT,
                BigDecimal.valueOf(1000)
        );

        when(clientService.existsById(testClientId)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.create(createRequest, testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("Client with id"));
        verify(clientService).existsById(testClientId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateSuccess() {
        AccountUpdateRequest updateRequest = new AccountUpdateRequest(
                AccountType.CREDIT,
                BigDecimal.valueOf(2000)
        );

        Account updatedAccount = Account.builder()
                .id(1L)
                .accountId(testAccountId)
                .accountType(AccountType.CREDIT)
                .balance(BigDecimal.valueOf(2000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .client(testClient)
                .build();

        AccountDto updatedAccountDto = AccountDto.builder()
                .id(testAccountId)
                .accountType(AccountType.CREDIT)
                .balance(BigDecimal.valueOf(2000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .build();

        when(accountRepository.findByAccountIdAndClientId(testAccountId, testClientId))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
        when(accountMapper.map(updatedAccount)).thenReturn(updatedAccountDto);

        AccountDto result = accountService.update(updateRequest, testAccountId, testClientId);

        assertEquals(updatedAccountDto, result);
        verify(accountRepository).findByAccountIdAndClientId(testAccountId, testClientId);
        verify(accountRepository).save(any(Account.class));
        verify(accountMapper).map(updatedAccount);
    }

    @Test
    void updateNotFound() {
        AccountUpdateRequest updateRequest = new AccountUpdateRequest(
                AccountType.CREDIT,
                BigDecimal.valueOf(2000)
        );

        when(accountRepository.findByAccountIdAndClientId(testAccountId, testClientId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.update(updateRequest, testAccountId, testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountIdAndClientId(testAccountId, testClientId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updatePartialSuccess() {
        AccountUpdateRequest updateRequest = new AccountUpdateRequest(
                null,
                BigDecimal.valueOf(2000)
        );

        Account updatedAccount = Account.builder()
                .id(1L)
                .accountId(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(2000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .client(testClient)
                .build();

        AccountDto updatedAccountDto = AccountDto.builder()
                .id(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(2000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .build();

        when(accountRepository.findByAccountIdAndClientId(testAccountId, testClientId))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
        when(accountMapper.map(updatedAccount)).thenReturn(updatedAccountDto);

        AccountDto result = accountService.update(updateRequest, testAccountId, testClientId);

        assertEquals(updatedAccountDto, result);
        assertEquals(AccountType.DEBIT, result.accountType());
        assertEquals(BigDecimal.valueOf(2000), result.balance());
        verify(accountRepository).findByAccountIdAndClientId(testAccountId, testClientId);
        verify(accountRepository).save(any(Account.class));
        verify(accountMapper).map(updatedAccount);
    }

    @Test
    void addAmountSuccess() {
        BigDecimal amountToAdd = BigDecimal.valueOf(500);
        Account updatedAccount = Account.builder()
                .id(1L)
                .accountId(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(1500))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .client(testClient)
                .build();

        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        accountService.addAmount(testAccountId, amountToAdd);

        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void addAmountNotFound() {
        BigDecimal amountToAdd = BigDecimal.valueOf(500);

        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.addAmount(testAccountId, amountToAdd));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateStatusSuccess() {
        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.updateStatus(testAccountId, AccountStatus.BLOCKED);

        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateStatusNotFound() {
        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.updateStatus(testAccountId, AccountStatus.BLOCKED));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateStatusSameStatus() {
        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.of(testAccount));

        accountService.updateStatus(testAccountId, AccountStatus.OPEN);

        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateAccountForBlockedTransactionSuccess() {
        BigDecimal amount = BigDecimal.valueOf(500);
        BigDecimal frozenAmount = BigDecimal.valueOf(500);
        Account updatedAccount = Account.builder()
                .id(1L)
                .accountId(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(500))
                .frozenAmount(BigDecimal.valueOf(500))
                .status(AccountStatus.BLOCKED)
                .client(testClient)
                .build();

        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        accountService.updateAccountForBlockedTransaction(testAccountId, amount, frozenAmount, AccountStatus.BLOCKED);

        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccountForBlockedTransactionNotFound() {
        BigDecimal amount = BigDecimal.valueOf(500);
        BigDecimal frozenAmount = BigDecimal.valueOf(500);

        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.updateAccountForBlockedTransaction(testAccountId, amount, frozenAmount, AccountStatus.BLOCKED));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountId(testAccountId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deleteSuccess() {
        when(accountRepository.findByAccountIdAndClientId(testAccountId, testClientId))
                .thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).deleteByAccountId(testAccountId);

        accountService.delete(testAccountId, testClientId);

        verify(accountRepository).findByAccountIdAndClientId(testAccountId, testClientId);
        verify(accountRepository).deleteByAccountId(testAccountId);
    }

    @Test
    void deleteNotFound() {
        when(accountRepository.findByAccountIdAndClientId(testAccountId, testClientId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.delete(testAccountId, testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountIdAndClientId(testAccountId, testClientId);
        verify(accountRepository, never()).deleteByAccountId(any());
    }

    @Test
    void existsByIdTrue() {
        when(accountRepository.existsByAccountId(testAccountId)).thenReturn(true);

        boolean result = accountService.existsById(testAccountId);

        assertTrue(result);
        verify(accountRepository).existsByAccountId(testAccountId);
    }

    @Test
    void existsByIdFalse() {
        when(accountRepository.existsByAccountId(testAccountId)).thenReturn(false);

        boolean result = accountService.existsById(testAccountId);

        assertFalse(result);
        verify(accountRepository).existsByAccountId(testAccountId);
    }

    @Test
    void getClientIdByAccountIdSuccess() {
        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.of(testAccount));

        Long result = accountService.getClientIdByAccountId(testAccountId);

        assertEquals(testClientId, result);
        verify(accountRepository).findByAccountId(testAccountId);
    }

    @Test
    void getClientIdByAccountIdNotFound() {
        when(accountRepository.findByAccountId(testAccountId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> accountService.getClientIdByAccountId(testAccountId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountRepository).findByAccountId(testAccountId);
    }
}
