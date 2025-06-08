package ru.t1.transactionvalidation.core.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionResultKafka;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionResultProducer {

    private final KafkaTemplate<String, TransactionResultKafka> kafkaTemplate;

    public void sendMessage(TransactionResultKafka payload) {
        Message<TransactionResultKafka> message = MessageBuilder
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