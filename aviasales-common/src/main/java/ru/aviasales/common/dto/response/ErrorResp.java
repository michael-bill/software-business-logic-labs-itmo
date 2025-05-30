package ru.aviasales.common.dto.response;


import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с ошибкой")
public class ErrorResp {
    @Schema(description = "Сообщение об ошибке")
    private String message;
    @Schema(description = "Время возникновения ошибки")
    private LocalDateTime timestamp;
}
