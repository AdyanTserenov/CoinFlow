package com.example.coinflow.controllers;

import com.example.coinflow.models.Transaction;
import com.example.coinflow.models.TransactionRequest;
import com.example.coinflow.models.TransactionResponse;
import com.example.coinflow.models.Category;
import com.example.coinflow.models.User;
import com.example.coinflow.repositories.CategoryRepository;
import com.example.coinflow.repositories.UserRepository;
import com.example.coinflow.services.TransactionService;
import com.example.coinflow.impls.UserDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Transactions", description = "API для управления транзакциями")
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TransactionController(TransactionService transactionService, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // Получить все транзакции пользователя
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Получить все транзакции пользователя",
        description = "Возвращает список всех транзакций текущего пользователя.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список транзакций получен успешно",
                content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public List<TransactionResponse> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactions(user).stream().map(this::toResponse).toList();
    }

    // Получить транзакцию по id
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Получить транзакцию по id",
        description = "Возвращает транзакцию по её идентификатору.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID транзакции", example = "1", required = true)
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Транзакция найдена",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Транзакция не найдена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return transactionService.getTransaction(id)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Создать транзакцию
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Создать транзакцию",
        description = "Создаёт новую транзакцию для текущего пользователя.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Данные транзакции",
            content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\n  \"amount\": 1500.00,\n  \"date\": \"2024-06-10T12:00:00\",\n  \"categoryId\": 1,\n  \"note\": \"Покупка в супермаркете\",\n  \"type\": \"EXPENSE\",\n  \"recurrence\": \"MONTHLY\",\n  \"nextOccurrence\": \"2024-07-10T12:00:00\"\n}"
                )
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Транзакция успешно создана",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неавторизован"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка валидации")
        }
    )
    public ResponseEntity<?> create(@RequestBody TransactionRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setCategory(category);
            transaction.setAmount(request.getAmount());
            transaction.setDate(request.getDate());
            transaction.setNote(request.getNote());
            transaction.setType(request.getType());
            transaction.setRecurrence(request.getRecurrence());
            transaction.setNextOccurrence(request.getNextOccurrence());
            return ResponseEntity.ok(toResponse(transactionService.createTransaction(transaction)));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body("Ошибка создания транзакции: " + ex.getMessage());
        }
    }

    // Обновить транзакцию
    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Обновить транзакцию",
        description = "Обновляет существующую транзакцию по id.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID транзакции", example = "1", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Обновлённые данные транзакции",
            content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\n  \"amount\": 2000.00,\n  \"date\": \"2024-06-15T12:00:00\",\n  \"categoryId\": 2,\n  \"note\": \"Обед\",\n  \"type\": \"EXPENSE\",\n  \"recurrence\": \"NONE\",\n  \"nextOccurrence\": null\n}"
                )
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Транзакция успешно обновлена",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неавторизован"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Транзакция не найдена")
        }
    )
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TransactionRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));
            Transaction transaction = new Transaction();
            transaction.setId(id);
            transaction.setUser(user);
            transaction.setCategory(category);
            transaction.setAmount(request.getAmount());
            transaction.setDate(request.getDate());
            transaction.setNote(request.getNote());
            transaction.setType(request.getType());
            transaction.setRecurrence(request.getRecurrence());
            transaction.setNextOccurrence(request.getNextOccurrence());
            return ResponseEntity.ok(toResponse(transactionService.updateTransaction(transaction)));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body("Ошибка обновления транзакции: " + ex.getMessage());
        }
    }

    // Удалить транзакцию
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Удалить транзакцию",
        description = "Удаляет транзакцию по id.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID транзакции", example = "1", required = true)
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Транзакция успешно удалена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неавторизован"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Транзакция не найдена")
        }
    )
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body("Ошибка удаления транзакции: " + ex.getMessage());
        }
    }

    // Фильтрация по типу (INCOME/EXPENSE)
    @GetMapping("/by-type")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Фильтрация транзакций по типу",
        description = "Возвращает транзакции пользователя по типу (INCOME/EXPENSE)",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "type", description = "Тип транзакции: INCOME или EXPENSE", example = "INCOME", required = true)
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список транзакций по типу получен успешно",
                content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public List<TransactionResponse> byType(@RequestParam String type, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactionsByType(user, type).stream().map(this::toResponse).toList();
    }

    // Фильтрация по категории
    @GetMapping("/by-category")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Фильтрация транзакций по категории",
        description = "Возвращает транзакции пользователя по категории.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "categoryId", description = "ID категории транзакции", example = "1", required = true)
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список транзакций по категории получен успешно",
                content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TransactionResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public List<TransactionResponse> byCategory(@RequestParam Long categoryId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Категория не найдена"));
        return transactionService.getUserTransactionsByCategory(user, category.getName()).stream().map(this::toResponse).toList();
    }

    // Маппер из Transaction в TransactionResponse
    private TransactionResponse toResponse(Transaction t) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(t.getId());
        dto.setAmount(t.getAmount());
        dto.setDate(t.getDate());
        dto.setCategoryId(t.getCategory().getId());
        dto.setNote(t.getNote());
        dto.setType(t.getType());
        dto.setRecurrence(t.getRecurrence());
        dto.setNextOccurrence(t.getNextOccurrence());
        return dto;
    }
}