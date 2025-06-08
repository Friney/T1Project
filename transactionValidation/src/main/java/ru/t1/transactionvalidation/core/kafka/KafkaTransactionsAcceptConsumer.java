package ru.t1.transactionvalidation.core.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionAcceptKafka;
import ru.t1.transactionvalidation.core.service.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionsAcceptConsumer {

    private final TransactionService transactionService;

    @KafkaListener(
            id = "${t1.kafka.group-id.transaction_accept}",
            topics = "${t1.kafka.topic.transaction_accept}",
            containerFactory = "kafkaListenerTransactionsAcceptContainerFactory"
    )
    public void listenTransactionsCreate(@Payload List<TransactionAcceptKafka> transactionCreateKafkaList,
                                         Acknowledgment ack,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received message -> topic: {}, size: {}", topic, transactionCreateKafkaList.size());
            transactionCreateKafkaList.forEach(transactionService::processIncomingTransaction);
        } catch (Exception e) {
            log.error("Failed to process message -> {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }
}
