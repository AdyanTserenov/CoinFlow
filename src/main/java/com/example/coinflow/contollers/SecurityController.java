package com.example.coinflow.contollers;

import com.example.coinflow.models.SigninRequest;
import com.example.coinflow.models.SignupRequest;
import com.example.coinflow.models.User;
import com.example.coinflow.repositories.UserRepository;
import com.example.coinflow.security.JwtCore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Security")
@RequiredArgsConstructor
public class SecurityController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с указанным именем пользователя, email и паролем " +
                    "Если имя пользователя или email уже существуют, возвращает ошибку",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная регистрация пользователя"),
                    @ApiResponse(responseCode = "400", description = "Ошибка: имя пользователя или email уже существуют")
            }
    )
    @PostMapping("/sign-up")
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

    @Operation(
            summary = "Авторизация пользователя",
            description = "Проверяет учетные данные пользователя и возвращает JWT токен, если авторизация успешна " +
                    "Возвращает статус 401, если учетные данные неверны",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная авторизация, возвращает JWT токен"),
                    @ApiResponse(responseCode = "401", description = "Ошибка: неверные учетные данные")
            }
    )
    @PostMapping("/sign-in")
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

    @Operation(
            summary = "OAuth2 Callback",
            description = "Обрабатывает OAuth2 callback и генерирует JWT токен для аутентифицированного пользователя",
            responses = {
            @ApiResponse(responseCode = "200", description = "JWT токен успешно сгенерирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутрення ошибка сервера")
            }
    )
    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> oauth2Callback(OAuth2AuthenticationToken authentication) {
        String username = authentication.getPrincipal().getAttribute("username");
        String email = authentication.getPrincipal().getAttribute("email");

        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("User '%s' not found", username)
        ));

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword("");
            userRepository.save(user);
        }

        String jwt = jwtCore.generateToken(new UsernamePasswordAuthenticationToken(user.getUsername(), null));

        return ResponseEntity.ok(jwt);
    }
}
