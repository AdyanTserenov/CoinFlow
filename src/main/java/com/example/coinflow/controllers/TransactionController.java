package com.example.coinflow.controllers;

import com.example.coinflow.models.Transaction;
import com.example.coinflow.models.User;
import com.example.coinflow.services.TransactionService;
import com.example.coinflow.repositories.UserRepository;
import com.example.coinflow.impls.UserDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    // Получить все транзакции пользователя
    @GetMapping
    @Operation(summary = "Получить все транзакции пользователя", description = "Возвращает список всех транзакций текущего пользователя.")
    @ApiResponse(responseCode = "200", description = "Список транзакций получен успешно")
    public List<Transaction> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactions(user);
    }

    // Получить транзакцию по id
    @GetMapping("/{id}")
    @Operation(summary = "Получить транзакцию по id", description = "Возвращает транзакцию по её идентификатору.")
    @ApiResponse(responseCode = "200", description = "Транзакция найдена")
    @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
    public ResponseEntity<Transaction> getById(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<Transaction> transaction = transactionService.getTransaction(id);
        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Создать транзакцию
    @PostMapping
    @Operation(summary = "Создать транзакцию", description = "Создаёт новую транзакцию для текущего пользователя.")
    @ApiResponse(responseCode = "200", description = "Транзакция успешно создана")
    public Transaction create(@RequestBody Transaction transaction, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        transaction.setUser(user);
        return transactionService.createTransaction(transaction);
    }

    // Обновить транзакцию
    @PutMapping("/{id}")
    @Operation(summary = "Обновить транзакцию", description = "Обновляет существующую транзакцию по id.")
    @ApiResponse(responseCode = "200", description = "Транзакция успешно обновлена")
    public ResponseEntity<Transaction> update(@PathVariable Long id, @RequestBody Transaction transaction, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        transaction.setId(id);
        transaction.setUser(user);
        return ResponseEntity.ok(transactionService.updateTransaction(transaction));
    }

    // Удалить транзакцию
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить транзакцию", description = "Удаляет транзакцию по id.")
    @ApiResponse(responseCode = "204", description = "Транзакция успешно удалена")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // Фильтрация по дате
    @GetMapping("/by-date")
    @Operation(summary = "Фильтрация транзакций по дате", description = "Возвращает транзакции пользователя за указанный период.")
    @ApiResponse(responseCode = "200", description = "Список транзакций за период получен успешно")
    public List<Transaction> byDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactionsByDate(user, start, end);
    }

    // Фильтрация по категории
    @GetMapping("/by-category")
    @Operation(summary = "Фильтрация транзакций по категории", description = "Возвращает транзакции пользователя по категории.")
    @ApiResponse(responseCode = "200", description = "Список транзакций по категории получен успешно")
    public List<Transaction> byCategory(@RequestParam String category, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactionsByCategory(user, category);
    }

    // Фильтрация по типу (INCOME/EXPENSE)
    @GetMapping("/by-type")
    @Operation(summary = "Фильтрация транзакций по типу", description = "Возвращает транзакции пользователя по типу (INCOME/EXPENSE).")
    @ApiResponse(responseCode = "200", description = "Список транзакций по типу получен успешно")
    public List<Transaction> byType(@RequestParam String type, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactionsByType(user, type);
    }
} 