package com.example.coinflow.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Модель категории транзакций")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор категории", example = "1")
    private Long id;

    @Column(nullable = false, length = 255)
    @Schema(description = "Название категории", example = "Еда")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Описание категории", example = "Расходы на продукты питания и рестораны")
    private String description;

    @Column(name = "is_default", nullable = false)
    @Schema(description = "Флаг, указывающий является ли категория стандартной", example = "true")
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "limit_amount")
    @Schema(description = "Лимит расходов по категории", example = "10000.00")
    private java.math.BigDecimal limit;
}