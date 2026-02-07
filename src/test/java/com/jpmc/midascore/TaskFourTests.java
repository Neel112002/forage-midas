package com.jpmc.midascore;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import com.jpmc.midascore.repository.UserAccountRepository;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskFourTests {
    static final Logger logger = LoggerFactory.getLogger(TaskFourTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void task_four_verifier() throws InterruptedException {
        // 1) Load initial users
        userPopulator.populate();

        // 2) Send all transactions to Kafka
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // 3) Allow processing time
        Thread.sleep(2000);

        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("use your debugger to find out what wilbur's balance is after all transactions are processed");
        logger.info("kill this test once you find the answer");

        // 4) Keep checking Wilbur's balance
        while (true) {
            Thread.sleep(2000);
            var wilburOpt = userAccountRepository.findByName("wilbur");
            if (wilburOpt.isPresent()) {
                var wilbur = wilburOpt.get();
                logger.info("Wilbur balance = {}", wilbur.getBalance());
            } else {
                logger.info("Wilbur not found yet");
            }
        }
    }
}
