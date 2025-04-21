package ru.aviasales.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.aviasales.common.dto.request.AuthReq;
import ru.aviasales.common.dto.response.UserResp;
import ru.aviasales.admin.service.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public UserResp signIn(@RequestBody AuthReq request) {
        return authService.signIn(request);
    }

}
