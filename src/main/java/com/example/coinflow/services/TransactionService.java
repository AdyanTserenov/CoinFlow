package com.example.coinflow.services;

import com.example.coinflow.models.Transaction;
import com.example.coinflow.models.User;
import com.example.coinflow.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public List<Transaction> getUserTransactions(User user) {
        return transactionRepository.findByUser(user);
    }

    public Optional<Transaction> getTransaction(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public List<Transaction> getUserTransactionsByDate(User user, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByUserAndDateBetween(user, start, end);
    }

    public List<Transaction> getUserTransactionsByCategory(User user, String category) {
        return transactionRepository.findByUserAndCategory(user, category);
    }

    public List<Transaction> getUserTransactionsByType(User user, String type) {
        return transactionRepository.findByUserAndType(user, type);
    }

    // Для планировщика: найти все повторяющиеся транзакции, которые нужно добавить
    public List<Transaction> getRecurringToProcess(LocalDateTime now) {
        return transactionRepository.findByRecurrenceNotAndNextOccurrenceBefore("NONE", now);
    }
} 