package ru.t1.transactionvalidation.api.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionCreateKafka;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionCreateRequest;
import ru.t1.transactionvalidation.core.kafka.KafkaTransactionCreateProducer;

@RestController
@RequestMapping("api/v1/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final KafkaTransactionCreateProducer kafkaTransactionCreateProducer;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void create(@Valid @RequestBody TransactionCreateRequest transactionCreateRequest, @PathVariable Long accountId) {
        TransactionCreateKafka transactionCreateKafka = TransactionCreateKafka.builder()
                .transactionTime(transactionCreateRequest.transactionTime())
                .amount(transactionCreateRequest.amount())
                .accountId(accountId)
                .build();

        kafkaTransactionCreateProducer.sendMessage(transactionCreateKafka);
    }

}
