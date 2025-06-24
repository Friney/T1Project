package ru.t1.accountservice.core.service.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.blacklist.ClientBlacklistStatus;
import ru.t1.accountservice.api.dto.transaction.TransactionAcceptKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateRequest;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.api.dto.transaction.TransactionResultKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionResultStatus;
import ru.t1.accountservice.api.dto.transaction.TransactionUpdateRequest;
import ru.t1.accountservice.core.entity.account.Account;
import ru.t1.accountservice.core.entity.account.AccountStatus;
import ru.t1.accountservice.core.entity.account.AccountType;
import ru.t1.accountservice.core.entity.client.ClientStatus;
import ru.t1.accountservice.core.entity.transaction.Transaction;
import ru.t1.accountservice.core.entity.transaction.TransactionStatus;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.kafka.KafkaTransactionAcceptProducer;
import ru.t1.accountservice.core.mapper.TransactionMapper;
import ru.t1.accountservice.core.repository.TransactionRepository;
import ru.t1.accountservice.core.service.account.AccountService;
import ru.t1.accountservice.core.service.blacklist.BlacklistStatusService;
import ru.t1.accountservice.core.service.client.ClientService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private ClientService clientService;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private KafkaTransactionAcceptProducer kafkaTransactionAcceptProducer;
    @Mock
    private BlacklistStatusService blacklistStatusService;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final long testTransactionId = 1L;
    private final long testAccountId = 1L;
    private final long testClientId = 1L;
    private final LocalDateTime testTime = LocalDateTime.now();
    private final BigDecimal testAmount = BigDecimal.valueOf(1000);

    @Value("${t1.transaction.max-rejected}")
    private long maxRejectedTransaction;

    private Transaction testTransaction;
    private TransactionDto testTransactionDto;
    private AccountDto testAccountDto;

    @BeforeEach
    void setUp() {
        Account testAccount = Account.builder()
                .id(testAccountId)
                .accountId(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(2000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .build();

        testAccountDto = AccountDto.builder()
                .id(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(2000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.OPEN)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .transactionId(testTransactionId)
                .transactionTime(testTime)
                .createTime(testTime)
                .amount(testAmount)
                .account(testAccount)
                .status(TransactionStatus.ACCEPTED)
                .build();

        testTransactionDto = TransactionDto.builder()
                .id(testTransactionId)
                .transactionTime(testTime)
                .amount(testAmount)
                .status(TransactionStatus.ACCEPTED)
                .build();
    }

    @Test
    void getAllSuccess() {
        List<Transaction> transactions = Collections.singletonList(testTransaction);
        List<TransactionDto> expectedDtos = Collections.singletonList(testTransactionDto);

        when(transactionRepository.findAllByAccountId(testAccountId)).thenReturn(transactions);
        when(transactionMapper.map(transactions)).thenReturn(expectedDtos);

        List<TransactionDto> result = transactionService.getAll(testAccountId);

        assertEquals(expectedDtos, result);
        verify(transactionRepository).findAllByAccountId(testAccountId);
        verify(transactionMapper).map(transactions);
    }

    @Test
    void getAllEmptyList() {
        when(transactionRepository.findAllByAccountId(testAccountId)).thenReturn(Collections.emptyList());
        when(transactionMapper.map(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<TransactionDto> result = transactionService.getAll(testAccountId);

        assertTrue(result.isEmpty());
        verify(transactionRepository).findAllByAccountId(testAccountId);
        verify(transactionMapper).map(Collections.emptyList());
    }

    @Test
    void getByIdSuccess() {
        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionMapper.map(testTransaction)).thenReturn(testTransactionDto);

        TransactionDto result = transactionService.getById(testTransactionId, testAccountId);

        assertEquals(testTransactionDto, result);
        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionMapper).map(testTransaction);
    }

    @Test
    void getByIdNotFound() {
        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> transactionService.getById(testTransactionId, testAccountId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
    }

    @Test
    void createSuccess() {
        TransactionCreateRequest createRequest = new TransactionCreateRequest(
                testAmount,
                testTime
        );

        when(accountService.existsById(testAccountId)).thenReturn(true);
        when(transactionRepository.getNextTransactionId()).thenReturn(testTransactionId);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.map(testTransaction)).thenReturn(testTransactionDto);

        TransactionDto result = transactionService.create(createRequest, testAccountId);

        assertEquals(testTransactionDto, result);
        verify(accountService).existsById(testAccountId);
        verify(transactionRepository).getNextTransactionId();
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).map(testTransaction);
    }

    @Test
    void createAccountNotFound() {
        TransactionCreateRequest createRequest = new TransactionCreateRequest(
                testAmount,
                testTime
        );

        when(accountService.existsById(testAccountId)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> transactionService.create(createRequest, testAccountId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(accountService).existsById(testAccountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processIncomingTransactionSuccess() {
        TransactionCreateKafka createKafka = TransactionCreateKafka.builder()
                .accountId(testAccountId)
                .transactionTime(testTime)
                .amount(testAmount)
                .build();

        when(accountService.getOnlyById(testAccountId)).thenReturn(testAccountDto);
        when(accountService.getClientIdByAccountId(testAccountId)).thenReturn(testClientId);
        when(clientService.getStatus(testClientId)).thenReturn(ClientStatus.UNBLOCKED);
        when(transactionRepository.getNextTransactionId()).thenReturn(testTransactionId);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.processIncomingTransaction(createKafka);

        verify(accountService).getOnlyById(testAccountId);
        verify(accountService).getClientIdByAccountId(testAccountId);
        verify(clientService).getStatus(testClientId);
        verify(accountService).addAmount(testAccountId, testAmount);
        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTransactionAcceptProducer).sendMessage(any(TransactionAcceptKafka.class));
    }

    @Test
    void processIncomingTransactionAccountNotOpen() {
        TransactionCreateKafka createKafka = TransactionCreateKafka.builder()
                .accountId(testAccountId)
                .transactionTime(testTime)
                .amount(testAmount)
                .build();

        AccountDto blockedAccountDto = AccountDto.builder()
                .id(testAccountId)
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(2000))
                .frozenAmount(BigDecimal.ZERO)
                .status(AccountStatus.BLOCKED)
                .build();

        when(accountService.getOnlyById(testAccountId)).thenReturn(blockedAccountDto);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> transactionService.processIncomingTransaction(createKafka));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not open"));
        verify(accountService).getOnlyById(testAccountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processIncomingTransactionClientBlocked() {
        TransactionCreateKafka createKafka = TransactionCreateKafka.builder()
                .accountId(testAccountId)
                .transactionTime(testTime)
                .amount(testAmount)
                .build();

        when(accountService.getOnlyById(testAccountId)).thenReturn(testAccountDto);
        when(accountService.getClientIdByAccountId(testAccountId)).thenReturn(testClientId);
        when(clientService.getStatus(testClientId)).thenReturn(ClientStatus.BLOCKED);
        when(transactionRepository.getNextTransactionId()).thenReturn(testTransactionId);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.processIncomingTransaction(createKafka);

        verify(accountService).getOnlyById(testAccountId);
        verify(accountService).getClientIdByAccountId(testAccountId);
        verify(clientService).getStatus(testClientId);
        verify(accountService).updateStatus(testAccountId, AccountStatus.BLOCKED);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processIncomingTransactionClientBlacklisted() {
        TransactionCreateKafka createKafka = TransactionCreateKafka.builder()
                .accountId(testAccountId)
                .transactionTime(testTime)
                .amount(testAmount)
                .build();

        when(accountService.getOnlyById(testAccountId)).thenReturn(testAccountDto);
        when(accountService.getClientIdByAccountId(testAccountId)).thenReturn(testClientId);
        when(clientService.getStatus(testClientId)).thenReturn(ClientStatus.UNKNOWN);
        when(blacklistStatusService.getBlacklistStatus(testClientId, testAccountId))
                .thenReturn(ClientBlacklistStatus.BLACKLISTED);
        when(transactionRepository.getNextTransactionId()).thenReturn(testTransactionId);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.processIncomingTransaction(createKafka);

        verify(accountService).getOnlyById(testAccountId);
        verify(accountService).getClientIdByAccountId(testAccountId);
        verify(clientService).getStatus(testClientId);
        verify(blacklistStatusService).getBlacklistStatus(testClientId, testAccountId);
        verify(accountService).updateStatus(testAccountId, AccountStatus.BLOCKED);
        verify(clientService).updateStatus(testClientId, ClientStatus.BLOCKED);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransactionResponseAccepted() {
        TransactionResultKafka resultKafka = TransactionResultKafka.builder()
                .accountId(testAccountId)
                .transactionId(testTransactionId)
                .transactionResultStatus(TransactionResultStatus.ACCEPTED)
                .build();

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.processTransactionResponse(resultKafka);

        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransactionResponseBlocked() {
        TransactionResultKafka resultKafka = TransactionResultKafka.builder()
                .accountId(testAccountId)
                .transactionId(testTransactionId)
                .transactionResultStatus(TransactionResultStatus.BLOCKED)
                .build();

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.processTransactionResponse(resultKafka);

        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(accountService).updateAccountForBlockedTransaction(
                testAccountId,
                testAmount.negate(),
                testAmount.abs(),
                AccountStatus.BLOCKED
        );
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransactionResponseRejected() {
        TransactionResultKafka resultKafka = TransactionResultKafka.builder()
                .accountId(testAccountId)
                .transactionId(testTransactionId)
                .transactionResultStatus(TransactionResultStatus.REJECTED)
                .build();

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.findAllByAccountIdAndStatus(testAccountId, TransactionStatus.REJECTED))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.processTransactionResponse(resultKafka);

        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(accountService).addAmount(testAccountId, testAmount.negate());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransactionResponseRejectedMaxReached() {
        TransactionResultKafka resultKafka = TransactionResultKafka.builder()
                .accountId(testAccountId)
                .transactionId(testTransactionId)
                .transactionResultStatus(TransactionResultStatus.REJECTED)
                .build();

        List<Transaction> rejectedTransactions = new ArrayList<>();
        for (int i = 0; i < maxRejectedTransaction; i++) {
            rejectedTransactions.add(testTransaction);
        }

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.findAllByAccountIdAndStatus(testAccountId, TransactionStatus.REJECTED))
                .thenReturn(rejectedTransactions);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.processTransactionResponse(resultKafka);

        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(accountService).addAmount(testAccountId, testAmount.negate());
        verify(accountService).updateStatus(testAccountId, AccountStatus.ARRESTED);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransactionResponseNotFound() {
        TransactionResultKafka resultKafka = TransactionResultKafka.builder()
                .accountId(testAccountId)
                .transactionId(testTransactionId)
                .transactionResultStatus(TransactionResultStatus.ACCEPTED)
                .build();

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> transactionService.processTransactionResponse(resultKafka));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void updateSuccess() {
        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(
                testAmount,
                testTime
        );

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.map(testTransaction)).thenReturn(testTransactionDto);

        TransactionDto result = transactionService.update(updateRequest, testTransactionId, testAccountId);

        assertEquals(testTransactionDto, result);
        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).map(testTransaction);
    }

    @Test
    void updateNotFound() {
        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(
                testAmount,
                testTime
        );

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> transactionService.update(updateRequest, testTransactionId, testAccountId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void updatePartialSuccess() {
        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(
                testAmount,
                null
        );

        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.map(testTransaction)).thenReturn(testTransactionDto);

        TransactionDto result = transactionService.update(updateRequest, testTransactionId, testAccountId);

        assertEquals(testTransactionDto, result);
        assertEquals(testTime, result.transactionTime());
        assertEquals(testAmount, result.amount());
        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).map(testTransaction);
    }

    @Test
    void deleteSuccess() {
        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.of(testTransaction));
        doNothing().when(transactionRepository).deleteByTransactionId(testTransactionId);

        transactionService.delete(testTransactionId, testAccountId);

        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionRepository).deleteByTransactionId(testTransactionId);
    }

    @Test
    void deleteNotFound() {
        when(transactionRepository.findByTransactionIdAndAccountId(testTransactionId, testAccountId))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> transactionService.delete(testTransactionId, testAccountId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(transactionRepository).findByTransactionIdAndAccountId(testTransactionId, testAccountId);
        verify(transactionRepository, never()).deleteByTransactionId(anyLong());
    }
}
