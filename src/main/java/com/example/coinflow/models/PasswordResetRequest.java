package com.example.coinflow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на сброс пароля")
public class PasswordResetRequest {
    @Schema(description = "Email пользователя, для которого нужно сбросить пароль", 
            example = "user@example.com", 
            required = true)
    private String email;
}
