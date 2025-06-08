package ru.t1.accountservice.core.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.accountservice.api.dto.transaction.TransactionResultKafka;
import ru.t1.accountservice.core.service.transaction.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionsResultConsumer {

    private final TransactionService transactionService;

    @KafkaListener(
            id = "${t1.kafka.group-id.transaction-result}",
            topics = "${t1.kafka.topic.transaction-result}",
            containerFactory = "kafkaListenerTransactionsResultContainerFactory"
    )
    public void listenTransactionsCreate(@Payload List<TransactionResultKafka> transactionCreateKafkaList,
                                         Acknowledgment ack,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received message -> topic: {}, size: {}", topic, transactionCreateKafkaList.size());
            transactionCreateKafkaList.forEach(transactionService::processTransactionResponse);
        } catch (Exception e) {
            log.error("Failed to process message -> {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }
}
