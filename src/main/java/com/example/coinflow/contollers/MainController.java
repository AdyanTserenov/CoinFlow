package com.example.coinflow.contollers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    @Operation(
            summary = "Выводит информацию о пользователе через OAuth2",
            description = "Возвращает информацию о пользователе, который вошел через OAuth2",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешно получена информация о пользователе"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/oauth2/user")
    public String oauth2UserAccess(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }
        String username = authentication.getPrincipal().getAttribute("name");
        return username != null ? username : "Имя пользователя не найдено";
    }
}
