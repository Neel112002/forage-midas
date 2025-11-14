package com.jpmc.midascore.component;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.jpmc.midascore.foundation.Transaction;

@Component
public class TransactionListener {

    private int counter = 0;

    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-core-consumer"
    )
    public void handleTransaction(Transaction transaction) {
        counter++;
        System.out.println("TX #" + counter + " → amount = " + transaction.getAmount());
    }
}
