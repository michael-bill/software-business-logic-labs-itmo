package ru.aviasales.admin.dto.response;

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
@Schema(description = "Категория продаж")
public class SalesCategoryResp {
    @Schema(description = "Идентификатор категории")
    private Long id;
    @Schema(description = "Версия категории (для оптимистичной блокировки)")
    private Long version;
    @Schema(description = "Название категории")
    private String name;
    @Schema(description = "Описание категории")
    private String description;
    @Schema(description = "Процент комиссии по умолчанию")
    private Double defaultCommissionPercent;
    @Schema(description = "Время создания категории")
    private LocalDateTime createdAt;
    @Schema(description = "Время последнего изменения категории")
    private LocalDateTime updatedAt;
}
