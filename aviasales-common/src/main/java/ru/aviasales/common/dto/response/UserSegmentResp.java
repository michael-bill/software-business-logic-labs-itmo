package ru.aviasales.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сегмент пользователей")
public class UserSegmentResp {
    @Schema(description = "Идентификатор сегмента")
    private Long id;
    @Schema(description = "Название сегмента")
    private String name;
    @Schema(description = "Описание сегмента")
    private String description;
    @Schema(description = "Примерное количество пользователей в сегменте")
    private Integer estimatedAmount;
}
