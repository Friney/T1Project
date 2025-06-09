package ru.t1.accountservice.core.service.transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.blacklist.ClientBlacklistStatus;
import ru.t1.accountservice.api.dto.transaction.TransactionAcceptKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateRequest;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.api.dto.transaction.TransactionResultKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionResultStatus;
import ru.t1.accountservice.api.dto.transaction.TransactionUpdateRequest;
import ru.t1.accountservice.core.annotation.Cached;
import ru.t1.accountservice.core.annotation.LogDataSourceError;
import ru.t1.accountservice.core.annotation.Metric;
import ru.t1.accountservice.core.entity.account.Account;
import ru.t1.accountservice.core.entity.account.AccountStatus;
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

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final ClientService clientService;
    private final TransactionMapper transactionMapper;
    private final KafkaTransactionAcceptProducer kafkaTransactionAcceptProducer;
    private final BlacklistStatusService blacklistStatusService;


    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getAll(long accountId) {
        return transactionMapper.map(transactionRepository.findAllByAccountId(accountId));
    }

    @Override
    @Cached(name = "transaction")
    @Transactional(readOnly = true)
    public TransactionDto getById(long id, long accountId) {
        return transactionMapper.map(getEntityById(id, accountId));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public TransactionDto create(TransactionCreateRequest transactionCreateRequest, long accountId) {
        if (!accountService.existsById(accountId)) {
            throw new ServiceException("Account with id " + accountId + " not found", HttpStatus.NOT_FOUND);
        }

        Long clientId = accountService.getClientIdByAccountId(accountId);

        TransactionStatus transactionStatus = TransactionStatus.ACCEPTED;

        ClientStatus clientStatus = clientService.getStatus(clientId);
        if (clientStatus.equals(ClientStatus.BLOCKED)) {
            transactionStatus = TransactionStatus.BLOCKED;
            accountService.updateStatus(accountId, AccountStatus.BLOCKED);
        } else if (clientStatus.equals(ClientStatus.UNKNOWN)) {
            if (blacklistStatusService.getBlacklistStatus(clientId, accountId).equals(ClientBlacklistStatus.BLACKLISTED)) {
                transactionStatus = TransactionStatus.REJECTED;
                accountService.updateStatus(accountId, AccountStatus.BLOCKED);
                clientService.updateStatus(clientId, ClientStatus.BLOCKED);
            }
        }

        Account account = Account.builder()
                .id(accountId)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionRepository.getNextTransactionId())
                .transactionTime(transactionCreateRequest.transactionTime())
                .createTime(LocalDateTime.now())
                .amount(transactionCreateRequest.amount())
                .account(account)
                .status(transactionStatus)
                .build();

        return transactionMapper.map(transactionRepository.save(transaction));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public void processIncomingTransaction(TransactionCreateKafka transactionCreateKafka) {
        AccountDto accountDto = accountService.getOnlyById(transactionCreateKafka.accountId());
        long accountId = accountDto.id();
        if (accountDto.status() != AccountStatus.OPEN) {
            throw new ServiceException("Account with id " + transactionCreateKafka.accountId() + " is not open", HttpStatus.BAD_REQUEST);
        }

        Long clientId = accountService.getClientIdByAccountId(accountId);

        TransactionStatus transactionStatus = TransactionStatus.REQUESTED;
        boolean needSendToKafka = true;

        ClientStatus clientStatus = clientService.getStatus(clientId);
        if (clientStatus.equals(ClientStatus.BLOCKED)) {
            transactionStatus = TransactionStatus.BLOCKED;
            accountService.updateStatus(accountId, AccountStatus.BLOCKED);
            needSendToKafka = false;
        } else if (clientStatus.equals(ClientStatus.UNKNOWN)) {
            if (blacklistStatusService.getBlacklistStatus(clientId, accountId).equals(ClientBlacklistStatus.BLACKLISTED)) {
                transactionStatus = TransactionStatus.REJECTED;
                accountService.updateStatus(accountId, AccountStatus.BLOCKED);
                clientService.updateStatus(clientId, ClientStatus.BLOCKED);
                needSendToKafka = false;
            }
        }

        Account account = Account.builder()
                .id(accountId)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionRepository.getNextTransactionId())
                .transactionTime(transactionCreateKafka.transactionTime())
                .createTime(LocalDateTime.now())
                .amount(transactionCreateKafka.amount())
                .account(account)
                .status(transactionStatus)
                .build();

        transactionRepository.save(transaction);

        if (needSendToKafka) {
            accountService.addAmount(accountId, transactionCreateKafka.amount());
            TransactionAcceptKafka transactionAcceptKafka = TransactionAcceptKafka.builder()
                    .clientId(clientId)
                    .accountId(accountId)
                    .transactionId(transaction.getTransactionId())
                    .timestamp(transaction.getCreateTime())
                    .transactionAmount(transaction.getAmount())
                    .accountBalance(accountDto.balance())
                    .accountType(accountDto.accountType())
                    .build();

            kafkaTransactionAcceptProducer.sendMessage(transactionAcceptKafka);
        }
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public void processTransactionResponse(TransactionResultKafka transactionResultKafka) {
        Long accountId = transactionResultKafka.accountId();
        Long transactionId = transactionResultKafka.transactionId();
        TransactionResultStatus transactionResultStatus = transactionResultKafka.transactionResultStatus();
        Transaction transaction = getEntityById(transactionId, accountId);

        if (transactionResultStatus == TransactionResultStatus.BLOCKED) {
            transaction.setStatus(TransactionStatus.BLOCKED);
            accountService.updateAccountForBlockedTransaction(
                    accountId,
                    transaction.getAmount().negate(),
                    transaction.getAmount().abs(),
                    AccountStatus.BLOCKED
            );
        } else if (transactionResultStatus == TransactionResultStatus.REJECTED) {
            transaction.setStatus(TransactionStatus.REJECTED);
            accountService.addAmount(accountId, transaction.getAmount().negate());
        } else {
            transaction.setStatus(TransactionStatus.ACCEPTED);
        }

        transactionRepository.save(transaction);
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public TransactionDto update(TransactionUpdateRequest transactionUpdateRequest, long id, long accountId) {
        Transaction transaction = getEntityById(id, accountId);
        updateTransactionFields(transaction, transactionUpdateRequest);
        return transactionMapper.map(transactionRepository.save(transaction));
    }

    private void updateTransactionFields(Transaction transaction, TransactionUpdateRequest transactionUpdateRequest) {
        Optional.ofNullable(transactionUpdateRequest.amount()).ifPresent(transaction::setAmount);
        Optional.ofNullable(transactionUpdateRequest.transactionTime()).ifPresent(transaction::setTransactionTime);
    }

    @Override
    @LogDataSourceError
    @Transactional
    public void delete(long id, long accountId) {
        getEntityById(id, accountId);
        transactionRepository.deleteByTransactionId(id);
    }

    @Transactional(readOnly = true)
    protected Transaction getEntityById(long id, long accountId) {
        return transactionRepository.findByTransactionIdAndAccountId(id, accountId)
                .orElseThrow(() -> new ServiceException("Transaction with id " + id + " not found for account with id " + accountId, HttpStatus.NOT_FOUND));
    }
}
