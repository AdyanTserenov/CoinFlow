package com.example.coinflow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию")
public class SignupRequest {
    @Schema(description = "Имя пользователя",
            example = "petrov_ivan",
            required = true)
    private String username;
    @Schema(description = "Email пользователя",
            example = "ipetrov@example.com",
            required = true)
    private String email;
    @Schema(description = "Пароль для входа",
            example = "vanya_1984",
            required = true)
    private String password;
}
