package com.example.coinflow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;

@Schema(description = "DTO категории транзакций")
public class CategoryDto {
    @Schema(description = "Уникальный идентификатор категории", example = "1")
    public Long id;
    @Schema(description = "Название категории", example = "Еда")
    public String name;
    @Schema(description = "Описание категории", example = "Расходы на продукты питания и рестораны")
    public String description;
    @Schema(description = "Флаг, указывающий является ли категория стандартной", example = "true")
    public boolean isDefault;
    @Schema(description = "ID пользователя, которому принадлежит категория", example = "5", nullable = true)
    public Long userId;
    @Schema(description = "Дата и время создания категории", example = "2025-05-10T01:01:27.077091+03:00")
    public ZonedDateTime createdAt;
    @Schema(description = "Дата и время последнего обновления категории", example = "2025-05-10T01:01:27.077091+03:00")
    public ZonedDateTime updatedAt;
    public CategoryDto(Long id, String name, String description, boolean isDefault, Long userId, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isDefault = isDefault;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public CategoryDto() {}
} 