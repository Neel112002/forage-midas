package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Transaction> transactionProducerFactory(KafkaProperties kafkaProperties) {
        // Start with Spring Boot’s default producer properties (including embedded Kafka bootstrap server)
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        // Explicitly use String for keys and JSON for Transaction values
        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                new JsonSerializer<Transaction>()
        );
    }

    @Bean
    public KafkaTemplate<String, Transaction> kafkaTemplate(
            ProducerFactory<String, Transaction> transactionProducerFactory
    ) {
        return new KafkaTemplate<>(transactionProducerFactory);
    }
}
