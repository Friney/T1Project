package ru.t1.accountservice.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaMetricProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(Object payload, String header) {
        Message<Object> message = MessageBuilder
                .withPayload(payload)
                .setHeader("logType", header)
                .build();

        kafkaTemplate.send(message);
    }
}