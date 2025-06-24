package ru.t1.transactionvalidation.core.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionCreateKafka;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionCreateRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionCreateProducer {

    private final KafkaTemplate<String, TransactionCreateRequest> kafkaTemplate;

    public void sendMessage(TransactionCreateKafka payload) {
        Message<TransactionCreateKafka> message = MessageBuilder
                .withPayload(payload)
                .build();

        try {
            kafkaTemplate.send(message);
            log.info("Message sent -> {}", payload);
        } catch (Exception e) {
            log.error("Failed to send message -> {}", e.getMessage());
        } finally {
            kafkaTemplate.flush();
        }
    }
}