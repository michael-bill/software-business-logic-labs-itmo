package ru.aviasales.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание пользователя")
public class UserCreateReq {

    @Schema(description = "Имя пользователя", example = "admin")
    @Size(min = 4, max = 50, message = "Имя пользователя должно содержать от 4 до 50 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String username;

    @Schema(description = "Пароль", example = "admin")
    @Size(min = 4, max = 255, message = "Длина пароля должна быть не менее 4 и не более 255 символов")
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    @Schema(description = "Роль пользователя", example = "ADMIN")
    @NotBlank(message = "Роль пользователя не может быть пустой")
    private Role role;

    public enum Role {
        COMMISSIONS,
        ADVERTISEMENTS,
        ADMIN
    }

}
