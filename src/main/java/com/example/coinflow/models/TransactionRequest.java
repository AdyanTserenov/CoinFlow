package com.example.coinflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Запрос на создание или обновление транзакции")
public class TransactionRequest {
    @Schema(description = "Сумма транзакции", example = "1500.00")
    private BigDecimal amount;

    @Schema(description = "Дата транзакции", example = "2024-06-10T12:00:00")
    private LocalDateTime date;

    @JsonProperty("categoryId")
    @Schema(description = "ID категории транзакции", example = "1")
    private Long categoryId;

    @Schema(description = "Заметка к транзакции", example = "Покупка в супермаркете")
    private String note;

    @Schema(description = "Тип транзакции: INCOME (доход) или EXPENSE (расход)", example = "EXPENSE")
    private String type;

    @Schema(description = "Периодичность: NONE, DAILY, WEEKLY, MONTHLY, YEARLY", example = "MONTHLY")
    private String recurrence;

    @Schema(description = "Дата следующего автосоздания для повторяющихся транзакций", example = "2024-07-10T12:00:00")
    private LocalDateTime nextOccurrence;
}