package com.example.coinflow.controllers;

import com.example.coinflow.models.Category;
import com.example.coinflow.models.User;
import com.example.coinflow.repositories.UserRepository;
import com.example.coinflow.services.CategoryService;
import com.example.coinflow.models.CategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@Tag(name = "Category", description = "API для управления категориями транзакций")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @Autowired
    public CategoryController(CategoryService categoryService, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    private CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isDefault(),
                category.getUser() != null ? category.getUser().getId() : null,
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getLimit()
        );
    }

    @Operation(
        summary = "Получить все категории",
        description = "Возвращает список всех категорий пользователя и стандартных категорий"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список категорий успешно получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не авторизован"
        )
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @Parameter(description = "Текущий авторизованный пользователь")
            @AuthenticationPrincipal UserDetails principal) {
        String username = principal.getUsername();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Category> categories = categoryService.getAllCategories(user);
        List<CategoryDto> dtos = categories.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(
        summary = "Создать новую категорию",
        description = "Создает новую пользовательскую категорию"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Категория успешно создана",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные категории"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не авторизован"
        )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryDto> createCategory(
            @Parameter(description = "Данные новой категории")
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Данные для создания категории",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Category.class),
                    examples = @ExampleObject(
                        name = "Пример создания категории",
                        value = """
                        {
                            \"name\": \"Развлечения\",
                            \"description\": \"Расходы на кино, театры, концерты\",
                            \"limit\": 10000.00
                        }
                        """
                    )
                )
            )
            @RequestBody Category category,
            @Parameter(description = "Текущий авторизованный пользователь")
            @AuthenticationPrincipal UserDetails principal) {
        if (category.getLimit() != null && category.getLimit().signum() < 0) {
            return ResponseEntity.badRequest().body(null);
        }
        String username = principal.getUsername();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category saved = categoryService.createCategory(category, user);
        return ResponseEntity.ok(toDto(saved));
    }

    @Operation(
        summary = "Обновить категорию",
        description = "Обновляет существующую пользовательскую категорию"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Категория успешно обновлена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные категории"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не авторизован"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Нет прав на обновление категории"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Категория не найдена"
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "ID категории для обновления", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Новые данные категории")
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Данные для обновления категории",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Category.class),
                    examples = @ExampleObject(
                        name = "Пример обновления категории",
                        value = """
                        {\"name\": \"Развлечения и досуг\",\"description\": \"Расходы на кино, театры, концерты и другие развлечения\",\"limit\": 15000.00} 
                        """
                    )
                )
            )
            @RequestBody Category category,
            @Parameter(description = "Текущий авторизованный пользователь")
            @AuthenticationPrincipal UserDetails principal) {
        if (category.getLimit() != null && category.getLimit().signum() < 0) {
            return ResponseEntity.badRequest().body("Лимит не может быть отрицательным");
        }
        try {
            String username = principal.getUsername();
            User user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Category updated = categoryService.updateCategory(id, category, user);
            return ResponseEntity.ok(toDto(updated));
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body("Нет прав на обновление категории");
            } else if (ex.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body("Категория не найдена");
            } else {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }
    }

    @Operation(
        summary = "Удалить категорию",
        description = "Удаляет пользовательскую категорию"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Категория успешно удалена"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не авторизован"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Нет прав на удаление категории"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Категория не найдена"
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID категории для удаления", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Текущий авторизованный пользователь")
            @AuthenticationPrincipal UserDetails principal) {
        try {
            String username = principal.getUsername();
            User user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            categoryService.deleteCategory(id, user);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body("Нет прав на удаление категории");
            } else if (ex.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body("Категория не найдена");
            } else {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }
    }
} 