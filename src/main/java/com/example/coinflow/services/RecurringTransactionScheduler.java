package com.example.coinflow.services;

import com.example.coinflow.models.Transaction;
import com.example.coinflow.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {
    private final TransactionRepository transactionRepository;

    @Scheduled(cron = "0 0 2 * * *") // каждый день в 2:00 ночи
    public void processRecurringTransactions() {
        List<Transaction> recurring = transactionRepository.findByRecurrenceNotAndNextOccurrenceBefore("NONE", LocalDateTime.now());
        for (Transaction t : recurring) {
            // Создать новую транзакцию на основе шаблона
            Transaction newTx = new Transaction();
            newTx.setUser(t.getUser());
            newTx.setAmount(t.getAmount());
            newTx.setDate(t.getNextOccurrence());
            newTx.setCategory(t.getCategory());
            newTx.setNote(t.getNote());
            newTx.setType(t.getType());
            newTx.setRecurrence("NONE"); // Новая транзакция — обычная, не повторяющаяся
            transactionRepository.save(newTx);

            // Обновить nextOccurrence у шаблона
            LocalDateTime next = switch (t.getRecurrence()) {
                case "DAILY" -> t.getNextOccurrence().plus(1, ChronoUnit.DAYS);
                case "WEEKLY" -> t.getNextOccurrence().plus(1, ChronoUnit.WEEKS);
                case "MONTHLY" -> t.getNextOccurrence().plus(1, ChronoUnit.MONTHS);
                case "YEARLY" -> t.getNextOccurrence().plus(1, ChronoUnit.YEARS);
                default -> null;
            };
            t.setNextOccurrence(next);
            transactionRepository.save(t);
        }
    }
} 