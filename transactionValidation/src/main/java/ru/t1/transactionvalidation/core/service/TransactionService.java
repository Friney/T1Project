package ru.t1.transactionvalidation.core.service;

import ru.t1.transactionvalidation.api.dto.transaction.TransactionAcceptKafka;

public interface TransactionService {

    void processIncomingTransaction(TransactionAcceptKafka transactionAcceptKafka);
}
