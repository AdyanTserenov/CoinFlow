package com.example.coinflow.contollers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/secured")
@Tag(name = "Main")
public class MainController {

    @Operation(
            summary = "Выводит имя авторизованного пользователя",
            description = "Вместе с запросом передается JWT токен, в ответ отправляется имя пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешно получено имя пользователя"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/user")
    public String userAccess(Principal principal) {
        System.out.println(principal);
        if (principal == null) {
            return null;
        }
        return principal.getName();
    }
}
