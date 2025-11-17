package com.jpmc.midascore;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import com.jpmc.midascore.repository.UserRepository;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
public class TaskThreeTests {

    static final Logger logger = LoggerFactory.getLogger(TaskThreeTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    // ⭐ ADD THIS
    @Autowired
    private UserRepository userRepository;

    @Test
    void task_three_verifier() throws InterruptedException {

        // Load initial users
        userPopulator.populate();

        // Load all test transactions
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");

        // Send each transaction through Kafka
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // Give Kafka listeners + DB some time
        Thread.sleep(2000);

        logger.info("----------------------------------------------------------");
        logger.info("use your debugger to find out what waldorf's balance is after all transactions are processed");
        logger.info("kill this test once you find the answer");
        logger.info("----------------------------------------------------------");

        // 🔁 Print Waldorf balance every 2 seconds
        while (true) {
            Thread.sleep(2000);

            var waldorf = userRepository.findByName("waldorf").get();
            logger.info("Waldorf balance = " + waldorf.getBalance());
        }
    }
}
