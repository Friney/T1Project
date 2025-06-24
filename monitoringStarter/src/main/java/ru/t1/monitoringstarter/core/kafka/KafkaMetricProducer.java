package ru.t1.monitoringstarter.core.kafka;

import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@RequiredArgsConstructor
public class KafkaMetricProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(Object payload, String header) throws ExecutionException, InterruptedException {
        Message<Object> message = MessageBuilder
                .withPayload(payload)
                .setHeader("logType", header)
                .build();
        try {
            kafkaTemplate.send(message).get();
            log.info("Message sent -> {}", payload);
        } finally {
            kafkaTemplate.flush();
        }
    }
}