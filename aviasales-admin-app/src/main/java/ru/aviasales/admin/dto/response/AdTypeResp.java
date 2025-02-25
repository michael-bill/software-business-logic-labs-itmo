package ru.aviasales.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Тип рекламы")
public class AdTypeResp {
    @Schema(description = "Идентификатор типа рекламы")
    private Long id;
    @Schema(description = "Название типа рекламы")
    private String name;
    @Schema(description = "Поддерживает ли сегментацию")
    private Boolean supportsSegmentation;
    @Schema(description = "Активен ли тип рекламы")
    private Boolean active;
}
