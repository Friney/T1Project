package ru.t1.transactionvalidation.core.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.transactionvalidation.api.dto.account.AccountType;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionAcceptKafka;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionResultKafka;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionResultStatus;
import ru.t1.transactionvalidation.core.kafka.KafkaTransactionResultProducer;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Value("${t1.transaction.limit-in-interval}")
    private Long limit;
    @Value("${t1.transaction.interval}")
    private Duration interval;
    private final Map<ClientAndAccountIds, List<TransactionTimestamp>> transactions = new ConcurrentHashMap<>();

    private final KafkaTransactionResultProducer kafkaTransactionResultProducer;

    @Override
    public void processIncomingTransaction(TransactionAcceptKafka transactionAcceptKafka) {
        ClientAndAccountIds clientAndAccountIds = new ClientAndAccountIds(transactionAcceptKafka.clientId(), transactionAcceptKafka.accountId());
        transactions.computeIfAbsent(clientAndAccountIds, k -> new ArrayList<>())
                .add(new TransactionTimestamp(transactionAcceptKafka.transactionId(), transactionAcceptKafka.timestamp()));

        List<TransactionTimestamp> transactionAcceptKafkaList = transactions.get(clientAndAccountIds);
        transactionAcceptKafkaList.removeIf(
                transaction -> transaction
                        .timestamp()
                        .isBefore(LocalDateTime.now().minus(interval))
        );

        transactions.put(clientAndAccountIds, transactionAcceptKafkaList);

        if (transactionAcceptKafkaList.size() >= limit) {
            transactionAcceptKafkaList.forEach(
                    transaction -> sendResult(clientAndAccountIds.accountId(), transaction.transactionId(), TransactionResultStatus.BLOCKED)
            );
            return;
        }

        TransactionResultStatus transactionResultStatus = getTransactionStatus(transactionAcceptKafka.transactionAmount(), transactionAcceptKafka.accountBalance(), transactionAcceptKafka.accountType());

        sendResult(transactionAcceptKafka.accountId(), transactionAcceptKafka.transactionId(), transactionResultStatus);
    }

    private TransactionResultStatus getTransactionStatus(BigDecimal transactionAmount, BigDecimal accountBalance, AccountType accountType) {
        TransactionResultStatus transactionResultStatus;
        if (accountBalance.compareTo(transactionAmount) < 0 && accountType != AccountType.CREDIT) {
            transactionResultStatus = TransactionResultStatus.REJECTED;
        } else {
            transactionResultStatus = TransactionResultStatus.ACCEPTED;
        }
        return transactionResultStatus;
    }

    private void sendResult(Long accountId, Long transactionId, TransactionResultStatus transactionResultStatus) {
        TransactionResultKafka answer = TransactionResultKafka.builder()
                .accountId(accountId)
                .transactionId(transactionId)
                .transactionResultStatus(transactionResultStatus)
                .build();

        kafkaTransactionResultProducer.sendMessage(answer);
    }

    record ClientAndAccountIds(
            Long clientId,
            Long accountId
    ) {
    }

    record TransactionTimestamp(
            Long transactionId,
            LocalDateTime timestamp
    ) {
    }
}
