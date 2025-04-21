package ru.aviasales.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesUnitReq {

    @NotNull(message = "Название единицы не может быть пустым")
    @Schema(description = "Название единицы")
    private String name;

    @NotNull(message = "Описание единицы не может быть пустым")
    @Schema(description = "Описание единицы")
    private String description;

    @NotNull(message = "Категория единицы не может быть пустой")
    @Schema(description = "Категория единицы")
    private Long categoryId;

    @Schema(description = "Процент комиссии")
    private Double commissionPercent;

}
