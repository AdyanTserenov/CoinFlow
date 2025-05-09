package com.example.coinflow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с данными транзакции без вложенного пользователя")
public class TransactionResponse {
    @Schema(description = "ID транзакции", example = "1")
    private Long id;

    @Schema(description = "Сумма транзакции", example = "1500.00")
    private BigDecimal amount;

    @Schema(description = "Дата транзакции", example = "2024-06-10T12:00:00")
    private LocalDateTime date;

    @Schema(description = "Категория транзакции", example = "Продукты")
    private String category;

    @Schema(description = "Заметка к транзакции", example = "Покупка в супермаркете")
    private String note;

    @Schema(description = "Тип транзакции: INCOME (доход) или EXPENSE (расход)", example = "EXPENSE")
    private String type;

    @Schema(description = "Периодичность: NONE, DAILY, WEEKLY, MONTHLY, YEARLY", example = "MONTHLY")
    private String recurrence;

    @Schema(description = "Дата следующего автосоздания для повторяющихся транзакций", example = "2024-07-10T12:00:00")
    private LocalDateTime nextOccurrence;
} 