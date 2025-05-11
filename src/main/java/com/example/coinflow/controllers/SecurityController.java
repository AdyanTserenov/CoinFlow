package com.example.coinflow.controllers;

import com.example.coinflow.models.*;
import com.example.coinflow.repositories.PasswordResetTokenRepository;
import com.example.coinflow.repositories.UserRepository;
import com.example.coinflow.security.JwtCore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Security", description = "API для управления авторизации")
@RequiredArgsConstructor
public class SecurityController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с указанным именем пользователя, email и паролем " +
                    "Если имя пользователя или email уже существуют, возвращает ошибку",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная регистрация пользователя"),
                    @ApiResponse(responseCode = "400", description = "Ошибка: имя пользователя или email уже существуют")
            }
    )
    ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different name");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
        }
        String hashed = passwordEncoder.encode(signupRequest.getPassword());

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashed);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Проверяет учетные данные пользователя и возвращает JWT токен, если авторизация успешна " +
                    "Возвращает статус 401, если учетные данные неверны",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная авторизация, возвращает JWT токен"),
                    @ApiResponse(responseCode = "401", description = "Ошибка: неверные учетные данные")
            }
    )
    ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Запрос на сброс пароля",
            description = "Отправляет email с токеном для сброса пароля на указанный email адрес. " +
                    "Ссылка действительна в течение 1 часа. " +
                    "Если пользователь с указанным email не найден, возвращает ошибку.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Токен для сброса пароля отправлен на email"),
                    @ApiResponse(responseCode = "404", description = "Пользователь с указанным email не найден")
            }
    )
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        // 1. Находит пользователя по email
        User user = userRepository.findUserByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(
                String.format("User with email '%s' not found", request.getEmail())
            ));

        // 2. Генерирует уникальный токен
        String token = UUID.randomUUID().toString();
        
        // 3. Создает запись в базе данных с токеном
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Токен действителен 1 час
        passwordResetTokenRepository.save(passwordResetToken);

        // 4. Отправляет email с ссылкой для сброса
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abtserenov@edu.hse.ru");
        message.setTo(request.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, copy the token below:\n" + token);
        mailSender.send(message);

        return ResponseEntity.ok("Password reset token sent to your email");
    }

    @PostMapping("/reset-password/confirm")
    @Operation(
            summary = "Подтверждение сброса пароля",
            description = "Устанавливает новый пароль для пользователя по полученному токену. " +
                    "Токен должен быть действительным и не истекшим (действителен 1 час). " +
                    "После успешного сброса пароля токен становится недействительным.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пароль успешно изменен"),
                    @ApiResponse(responseCode = "400", description = "Неверный или истекший токен")
            }
    )
    public ResponseEntity<?> confirmResetPassword(@RequestParam String token, @RequestBody String newPassword) {
        System.out.println("Password received for reset: '" + newPassword + "'");
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null || passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }

        String hashed = passwordEncoder.encode(newPassword);
        User user = passwordResetToken.getUser();
        System.out.println("Reset password for user: " + user.getUsername() + ", new hash: " + hashed);
        user.setPassword(hashed);
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);

        return ResponseEntity.ok("Password has been reset successfully");
    }
}
