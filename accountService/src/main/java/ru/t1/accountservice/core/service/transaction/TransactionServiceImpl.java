package ru.t1.accountservice.core.service.transaction;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateRequest;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.api.dto.transaction.TransactionUpdateRequest;
import ru.t1.accountservice.core.entity.account.Account;
import ru.t1.accountservice.core.entity.transaction.Transaction;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.TransactionMapper;
import ru.t1.accountservice.core.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;


    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getAll(long accountId) {
        return transactionMapper.map(transactionRepository.findAllByAccountId(accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getById(long id, long accountId) {
        return transactionMapper.map(getEntityById(id, accountId));
    }


    @Transactional(readOnly = true)
    protected Transaction getEntityById(long id, long accountId) {
        return transactionRepository.findByIdAndAccountId(id, accountId)
                .orElseThrow(() -> new ServiceException("Transaction with id " + id + " not found for account with id " + accountId, HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public TransactionDto create(TransactionCreateRequest transactionCreateRequest, long accountId) {
        Account account = Account.builder()
                .id(accountId)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionTime(transactionCreateRequest.transactionTime())
                .amount(transactionCreateRequest.amount())
                .account(account)
                .build();

        return transactionMapper.map(transactionRepository.save(transaction));
    }

    @Override
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
    @Transactional
    public void delete(long id, long accountId) {
        getEntityById(id, accountId);
        transactionRepository.deleteById(id);
    }
}
