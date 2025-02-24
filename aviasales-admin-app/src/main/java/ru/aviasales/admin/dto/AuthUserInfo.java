package ru.aviasales.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserInfo {
    @Schema(description = "Имя пользователя")
    private String username;
    @Schema(description = "Роль пользователя в системе")
    private String role;
    @Schema(description = "Токен для доступа к API")
    private String token;
}
