package ru.t1.transactionvalidation.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionCreateRequest;

@Service
@RequiredArgsConstructor
public class KafkaTransactionCreateProducer {

    private final KafkaTemplate<String, TransactionCreateRequest> kafkaTemplate;

    public void sendMessage(TransactionCreateRequest payload) {
        Message<TransactionCreateRequest> message = MessageBuilder
                .withPayload(payload)
                .build();

        kafkaTemplate.send(message);
    }
}