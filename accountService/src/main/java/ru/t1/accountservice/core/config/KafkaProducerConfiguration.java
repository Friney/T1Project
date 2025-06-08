package ru.t1.accountservice.core.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.t1.accountservice.api.dto.transaction.TransactionAcceptKafka;

@Configuration
public class KafkaProducerConfiguration {

    @Value("${t1.kafka.topic.metrics}")
    private String metricsTopicName;
    @Value("${t1.kafka.topic.transaction-accept}")
    private String transactionAcceptTopicName;
    @Value("${t1.kafka.bootstrap-servers}")
    private String servers;
    @Value("${t1.kafka.producer.retries}")
    private int retries;
    @Value("${t1.kafka.producer.retry-backoff-ms}")
    private int retryBackoffMs;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = getConfigs();
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String, TransactionAcceptKafka> transactionAcceptproducerFactory() {
        Map<String, Object> config = getConfigs();
        return new DefaultKafkaProducerFactory<>(config);
    }


    @Bean
    public KafkaTemplate<String, Object> kafkaTemplateMetrics(ProducerFactory<String, Object> producerFactory) {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(metricsTopicName);
        return kafkaTemplate;
    }

    @Bean
    public KafkaTemplate<String, TransactionAcceptKafka> kafkaTemplateTransactionAccept(ProducerFactory<String, TransactionAcceptKafka> producerFactory) {
        KafkaTemplate<String, TransactionAcceptKafka> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(transactionAcceptTopicName);
        return kafkaTemplate;
    }

    private Map<String, Object> getConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.RETRIES_CONFIG, retries);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return config;
    }
}
