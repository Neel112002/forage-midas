package com.jpmc.midascore;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    // Intentionally empty.
    // Spring Boot Kafka auto-configuration +
    // src/test/resources/application.properties
    // handle all consumer settings.
}
