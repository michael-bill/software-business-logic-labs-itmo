package ru.aviasales.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.aviasales.admin.dao.entity.User;
import ru.aviasales.admin.dto.request.UserCreateReq;
import ru.aviasales.admin.dto.response.UserResp;
import ru.aviasales.admin.exception.NoPermissionException;
import ru.aviasales.admin.service.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User")
public class UserController {

    private final AuthService authService;

    @Operation(summary = "Создание пользователя (только админам)")
    @PostMapping
    public UserResp signUp(
            @AuthenticationPrincipal User user,
            @RequestBody UserCreateReq request
    ) {
        if (user.getRole() != User.Role.ADMIN) {
            throw new NoPermissionException("Нет прав для создания пользователя");
        }
        return authService.createUser(request);
    }

}
