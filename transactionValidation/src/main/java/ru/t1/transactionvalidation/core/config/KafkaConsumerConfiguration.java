package ru.t1.transactionvalidation.core.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import ru.t1.transactionvalidation.api.dto.transaction.TransactionAcceptKafka;
import ru.t1.transactionvalidation.core.kafka.MessageDeserializer;

@Slf4j
@Configuration
public class KafkaConsumerConfiguration {

    @Value("${t1.kafka.group-id.transaction_accept}")
    private String transactionsAcceptGroupId;
    @Value("${t1.kafka.bootstrap-servers}")
    private String servers;
    @Value("${t1.kafka.consumer.session-timeout-ms}")
    private String sessionTimeout;
    @Value("${t1.kafka.consumer.max.partition-fetch-bytes}")
    private String maxPartitionFetchBytes;
    @Value("${t1.kafka.consumer.max.poll.records:1}")
    private String maxPollRecords;
    @Value("${t1.kafka.consumer.max.poll.interval-ms:300000}")
    private String maxPollIntervalsMs;
    @Value("${t1.kafka.consumer.heartbeat.interval}")
    private String heartbeatInterval;


    @Bean
    public ConsumerFactory<String, TransactionAcceptKafka> transactionsAcceptConsumerFactory() {
        Map<String, Object> config = getConsumerConfig();
        DefaultKafkaConsumerFactory<String, TransactionAcceptKafka> factory = new DefaultKafkaConsumerFactory<>(config);
        factory.setKeyDeserializer(new StringDeserializer());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionAcceptKafka> kafkaListenerTransactionsAcceptContainerFactory(@Qualifier("transactionsAcceptConsumerFactory") ConsumerFactory<String, TransactionAcceptKafka> transactionsCreateConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TransactionAcceptKafka> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factoryBuilder(transactionsCreateConsumerFactory, factory);
        return factory;
    }

    private Map<String, Object> getConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, transactionsAcceptGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionAcceptKafka.class.getName());
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatInterval);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class.getName());
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, MessageDeserializer.class);
        return config;
    }

    private <T> void factoryBuilder(ConsumerFactory<String, T> consumerFactory, ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.getContainerProperties().setMicrometerEnabled(true);
        factory.setCommonErrorHandler(errorHandler());
    }

    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler =
                new DefaultErrorHandler(new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((consumerRecord, ex, deliveryAttempt) ->
                log.error("RetryListeners message = {}, offset = {} deliveryAttempt = {}", ex.getMessage(), consumerRecord.offset(), deliveryAttempt));
        return handler;
    }
}
