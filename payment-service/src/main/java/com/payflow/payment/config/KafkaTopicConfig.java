package com.payflow.payment.config;

import com.payflow.common.model.event.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private static final int DEFAULT_PARTITIONS = 3;
    private static final int DEFAULT_REPLICAS = 1;

    @Bean
    public NewTopic paymentInitiatedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_INITIATED)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_COMPLETED)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic paymentCompletedDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_COMPLETED + KafkaTopics.DLT_SUFFIX)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic walletDebitedTopic() {
        return TopicBuilder.name(KafkaTopics.WALLET_DEBITED)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic walletCreditedTopic() {
        return TopicBuilder.name(KafkaTopics.WALLET_CREDITED)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic ledgerRecordedTopic() {
        return TopicBuilder.name(KafkaTopics.LEDGER_RECORDED)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }
}
