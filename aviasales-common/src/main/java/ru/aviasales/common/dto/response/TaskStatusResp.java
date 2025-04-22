package ru.aviasales.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.aviasales.common.domain.TaskStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статус обработки задачи")
public class TaskStatusResp {

    @Schema(description = "Идентификатор задачи")
    private String taskId;

    @Schema(description = "Текущий статус задачи")
    private TaskStatus status;

    @Schema(description = "Сообщение об ошибке (если статус FAIL)")
    private String errorMessage;

    @Schema(description = "Время создания задачи")
    private LocalDateTime createdAt;

    @Schema(description = "Время последнего обновления статуса")
    private LocalDateTime updatedAt;
}
