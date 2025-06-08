package ru.t1.accountservice.core.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.accountservice.api.dto.transaction.TransactionAcceptKafka;
import ru.t1.accountservice.core.exception.ServiceException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionAcceptProducer {

    private final KafkaTemplate<String, TransactionAcceptKafka> kafkaTemplate;

    public void sendMessage(TransactionAcceptKafka payload) {
        Message<TransactionAcceptKafka> message = MessageBuilder
                .withPayload(payload)
                .build();

        try {
            kafkaTemplate.send(message);
            log.info("Message sent -> {}", payload);
        } catch (Exception e) {
            log.error("Failed to send message -> {}", e.getMessage());
            throw new ServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            kafkaTemplate.flush();
        }
    }
}
