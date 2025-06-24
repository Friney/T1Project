package ru.t1.transactionvalidation.core.config;

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
import ru.t1.transactionvalidation.api.dto.transaction.TransactionCreateRequest;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionResultKafka;

@Configuration
public class KafkaProducerConfiguration {

    @Value("${t1.kafka.topic.transactions}")
    private String transactionsCreateTopicName;
    @Value("${t1.kafka.topic.transaction_result}")
    private String transactionsResultTopicName;
    @Value("${t1.kafka.bootstrap-servers}")
    private String servers;
    @Value("${t1.kafka.producer.retries}")
    private int retries;
    @Value("${t1.kafka.producer.retry-backoff-ms}")
    private int retryBackoffMs;

    @Bean
    public <T> ProducerFactory<String, T> producerFactory() {
        Map<String, Object> config = getProducerConfig();
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String, TransactionCreateRequest> producerCreateFactory() {
        return producerFactory();
    }

    @Bean
    public ProducerFactory<String, TransactionResultKafka> producerResultFactory() {
        return producerFactory();
    }

    @Bean
    public KafkaTemplate<String, TransactionCreateRequest> kafkaTemplateTransactionCreate(ProducerFactory<String, TransactionCreateRequest> producerFactory) {
        KafkaTemplate<String, TransactionCreateRequest> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(transactionsCreateTopicName);
        return kafkaTemplate;
    }

    @Bean
    public KafkaTemplate<String, TransactionResultKafka> kafkaTemplateTransactionResult(ProducerFactory<String, TransactionResultKafka> producerFactory) {
        KafkaTemplate<String, TransactionResultKafka> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(transactionsResultTopicName);
        return kafkaTemplate;
    }

    private Map<String, Object> getProducerConfig() {
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
