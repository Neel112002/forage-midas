package com.jpmc.midascore.component;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserAccount;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserAccountRepository;

@Service
public class TransactionService {

    private static final String INCENTIVE_URL = "http://localhost:8080/incentive";

    private final UserAccountRepository userAccountRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;

    public TransactionService(
            UserAccountRepository userAccountRepository,
            TransactionRecordRepository transactionRecordRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        // IMPORTANT: local RestTemplate (no Spring bean dependency)
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validate and process a single transaction coming from Kafka.
     * If invalid, it is ignored and the DB is not modified.
     */
    @Transactional
    public void process(Transaction tx) {

        // --- 1. Read data from Transaction POJO ---
        long senderId = tx.getSenderId();
        long recipientId = tx.getRecipientId();
        BigDecimal amount = BigDecimal.valueOf(tx.getAmount());

        // --- 2. Validate sender/recipient exist ---
        Optional<UserAccount> senderOpt = userAccountRepository.findById(senderId);
        Optional<UserAccount> recipientOpt = userAccountRepository.findById(recipientId);

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return;
        }

        UserAccount sender = senderOpt.get();
        UserAccount recipient = recipientOpt.get();

        // --- 3. Validate sufficient balance ---
        if (sender.getBalance().compareTo(amount) < 0) {
            return;
        }

        // --- 4. Call Incentive API (Task 4) ---
        BigDecimal incentiveAmount = BigDecimal.ZERO;
        try {
            Incentive incentive = restTemplate.postForObject(
                    INCENTIVE_URL,
                    tx,
                    Incentive.class
            );

            if (incentive != null) {
                incentiveAmount = BigDecimal.valueOf(incentive.getAmount());
            }
        } catch (Exception e) {
            // incentive service failure → proceed with 0 incentive
            incentiveAmount = BigDecimal.ZERO;
        }

        // --- 5. Apply debits/credits ---
        // Sender loses ONLY the transaction amount
        sender.setBalance(sender.getBalance().subtract(amount));

        // Recipient gains transaction amount + incentive
        recipient.setBalance(
                recipient.getBalance()
                        .add(amount)
                        .add(incentiveAmount)
        );

        // --- 6. Persist updated users ---
        userAccountRepository.save(sender);
        userAccountRepository.save(recipient);

        // --- 7. Persist TransactionRecord with incentive ---
        TransactionRecord record =
                new TransactionRecord(sender, recipient, amount, incentiveAmount);

        transactionRecordRepository.save(record);
    }
}
