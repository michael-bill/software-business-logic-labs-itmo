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
@Schema(description = "Единица продаж")
public class SalesUnitResp {
    @Schema(description = "Идентификатор единицы")
    private Long id;
    @Schema(description = "Версия единицы (для оптимистичной блокировки)")
    private Long version;
    @Schema(description = "Название единицы")
    private String name;
    @Schema(description = "Описание единицы")
    private String description;
    @Schema(description = "Категория единицы")
    private SalesCategoryResp category;
    @Schema(description = "Процент комиссии")
    private Long actualCommission;
    @Schema(description = "Время создания единицы")
    private LocalDateTime createdAt;
    @Schema(description = "Время последнего изменения единицы")
    private LocalDateTime updatedAt;
}
