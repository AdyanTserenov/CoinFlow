package com.example.coinflow.repositories;

import com.example.coinflow.models.Transaction;
import com.example.coinflow.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserAndDateBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByUserAndCategory(User user, String category);
    List<Transaction> findByUserAndType(User user, String type);
    List<Transaction> findByRecurrenceNotAndNextOccurrenceBefore(String recurrence, LocalDateTime now);
} 