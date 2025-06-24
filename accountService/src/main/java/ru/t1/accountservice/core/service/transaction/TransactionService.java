package ru.t1.accountservice.core.service.transaction;

import java.util.List;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateRequest;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.api.dto.transaction.TransactionResultKafka;
import ru.t1.accountservice.api.dto.transaction.TransactionUpdateRequest;

public interface TransactionService {

    List<TransactionDto> getAll(long accountId);

    TransactionDto getById(long id, long accountId);

    TransactionDto create(TransactionCreateRequest transactionCreateRequest, long accountId);

    void processIncomingTransaction(TransactionCreateKafka transactionCreateKafka);

    void processTransactionResponse(TransactionResultKafka transactionResultKafka);

    TransactionDto update(TransactionUpdateRequest transactionUpdateRequest, long id, long accountId);

    void delete(long id, long accountId);
}
