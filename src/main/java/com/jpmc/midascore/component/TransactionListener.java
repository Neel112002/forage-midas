package com.jpmc.midascore.component;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.jpmc.midascore.foundation.Transaction;

@Component
public class TransactionListener {

    private final TransactionService service;

    public TransactionListener(TransactionService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-core"
    )
    public void receive(Transaction tx) {

        // 🔥 DEBUG: proves Kafka consumer is working
        System.out.println("🔥 KAFKA CONSUMED TRANSACTION: " + tx);

        service.process(tx);
    }
}
