package com.jpmc.midascore.component;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserAccount;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserAccountRepository;

@Service
public class TransactionService {

    private final UserAccountRepository userAccountRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionService(UserAccountRepository userAccountRepository,
                              TransactionRecordRepository transactionRecordRepository) {
        this.userAccountRepository = userAccountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    public void process(Transaction tx) {

        // USE THE REAL GETTERS FROM YOUR Transaction CLASS
        long senderId = tx.getSenderId();
        long recipientId = tx.getRecipientId();
        BigDecimal amount = BigDecimal.valueOf(tx.getAmount());

        // 1) Validate both users exist
        Optional<UserAccount> senderOpt = userAccountRepository.findById(senderId);
        Optional<UserAccount> recipientOpt = userAccountRepository.findById(recipientId);

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // discard
        }

        UserAccount sender = senderOpt.get();
        UserAccount recipient = recipientOpt.get();

        // 2) Check sender has enough balance
        if (sender.getBalance().compareTo(amount) < 0) {
            return; // insufficient funds
        }

        // 3) Apply transaction
        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));

        // Save updated users
        userAccountRepository.save(sender);
        userAccountRepository.save(recipient);

        // Save transaction record
        TransactionRecord record = new TransactionRecord(sender, recipient, amount);
        transactionRecordRepository.save(record);
    }
}
