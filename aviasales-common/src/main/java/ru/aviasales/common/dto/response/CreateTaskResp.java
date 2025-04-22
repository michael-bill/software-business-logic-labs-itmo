package ru.aviasales.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на запрос создания задачи")
public class CreateTaskResp {

    @Schema(description = "Сообщение о результате")
    private String message;

    @Schema(description = "Идентификатор созданной задачи для отслеживания")
    private String taskId;
}
