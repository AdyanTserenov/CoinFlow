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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import com.example.coinflow.models.TransactionRequest;
import com.example.coinflow.models.TransactionResponse;
import com.example.coinflow.repositories.CategoryRepository;
import com.example.coinflow.models.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "API для управления транзакциями")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // Получить все транзакции пользователя
    @GetMapping
    @Operation(
        summary = "Получить все транзакции пользователя",
        description = "Возвращает список всех транзакций текущего пользователя.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список транзакций получен успешно",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public List<TransactionResponse> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactions(user).stream().map(this::toResponse).toList();
    }

    // Получить транзакцию по id
    @GetMapping("/{id}")
    @Operation(
        summary = "Получить транзакцию по id",
        description = "Возвращает транзакцию по её идентификатору.",
        parameters = {
            @Parameter(name = "id", description = "ID транзакции", example = "1", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Транзакция найдена",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Транзакция не найдена"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<Transaction> transaction = transactionService.getTransaction(id);
        return transaction.map(t -> ResponseEntity.ok(toResponse(t))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Создать транзакцию
    @PostMapping
    @Operation(
        summary = "Создать транзакцию",
        description = "Создаёт новую транзакцию для текущего пользователя.",
        requestBody = @RequestBody(
            required = true,
            description = "Данные транзакции",
            content = @Content(
                schema = @Schema(implementation = TransactionRequest.class),
                examples = @ExampleObject(
                    value = "{\n  \"amount\": 1500.00,\n  \"date\": \"2024-06-10T12:00:00\",\n  \"categoryId\": 1,\n  \"note\": \"Покупка в супермаркете\",\n  \"type\": \"EXPENSE\",\n  \"recurrence\": \"MONTHLY\",\n  \"nextOccurrence\": \"2024-07-10T12:00:00\"\n}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Транзакция успешно создана",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
        }
    )
    public ResponseEntity<?> create(@RequestBody TransactionRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
            System.out.println("Request: " + request);
            System.out.println("categoryId: " + request.getCategoryId());
            System.out.println("amount: " + request.getAmount());
            System.out.println("date: " + request.getDate());
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
    @Operation(
        summary = "Обновить транзакцию",
        description = "Обновляет существующую транзакцию по id.",
        parameters = {
            @Parameter(name = "id", description = "ID транзакции", example = "1", required = true)
        },
        requestBody = @RequestBody(
            required = true,
            description = "Обновлённые данные транзакции",
            content = @Content(
                schema = @Schema(implementation = TransactionRequest.class),
                examples = @ExampleObject(
                    value = "{\n  \"amount\": 2000.00,\n  \"date\": \"2024-06-15T12:00:00\",\n  \"category\": \"Кафе\",\n  \"note\": \"Обед\",\n  \"type\": \"EXPENSE\",\n  \"recurrence\": \"NONE\",\n  \"nextOccurrence\": null\n}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Транзакция успешно обновлена",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
        }
    )
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id, @RequestBody TransactionRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setUser(user);
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setCategory(category);
        transaction.setNote(request.getNote());
        transaction.setType(request.getType());
        transaction.setRecurrence(request.getRecurrence());
        transaction.setNextOccurrence(request.getNextOccurrence());
        return ResponseEntity.ok(toResponse(transactionService.updateTransaction(transaction)));
    }

    // Удалить транзакцию
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить транзакцию",
        description = "Удаляет транзакцию по id.",
        parameters = {
            @Parameter(name = "id", description = "ID транзакции", example = "1", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "Транзакция успешно удалена"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
        }
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // Фильтрация по дате
    @GetMapping("/by-date")
    @Operation(
        summary = "Фильтрация транзакций по дате",
        description = "Возвращает транзакции пользователя за указанный период.",
        parameters = {
            @Parameter(name = "start", description = "Начальная дата (ISO 8601)", example = "2024-06-01T00:00:00", required = true),
            @Parameter(name = "end", description = "Конечная дата (ISO 8601)", example = "2024-06-30T23:59:59", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Список транзакций за период получен успешно",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public List<TransactionResponse> byDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactionsByDate(user, start, end).stream().map(this::toResponse).toList();
    }

    // Фильтрация по категории
    @GetMapping("/by-category")
    @Operation(
        summary = "Фильтрация транзакций по категории",
        description = "Возвращает транзакции пользователя по категории.",
        parameters = {
            @Parameter(name = "category", description = "Категория транзакции", example = "Продукты", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Список транзакций по категории получен успешно",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public List<TransactionResponse> byCategory(@RequestParam String category, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactionsByCategory(user, category).stream().map(this::toResponse).toList();
    }

    // Фильтрация по типу (INCOME/EXPENSE)
    @GetMapping("/by-type")
    @Operation(
        summary = "Фильтрация транзакций по типу",
        description = "Возвращает транзакции пользователя по типу (INCOME/EXPENSE).",
        parameters = {
            @Parameter(name = "type", description = "Тип транзакции: INCOME или EXPENSE", example = "INCOME", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Список транзакций по типу получен успешно",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
        }
    )
    public List<TransactionResponse> byType(@RequestParam String type, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();
        return transactionService.getUserTransactionsByType(user, type).stream().map(this::toResponse).toList();
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

    @PostMapping("/debug")
    public void debug(@RequestBody String raw) {
        System.out.println("RAW JSON: " + raw);
    }
} 