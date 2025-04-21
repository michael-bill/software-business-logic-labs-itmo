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
@Schema(description = "Запрос на создание категории")
public class SalesCategoryReq {

    @NotNull(message = "Название категории не может быть пустым")
    @Schema(description = "Название категории")
    private String name;

    @NotNull(message = "Описание категории не может быть пустым")
    @Schema(description = "Описание категории")
    private String description;

    @NotNull(message = "Процент комиссии по умолчанию не может быть пустым")
    @Schema(description = "Процент комиссии по умолчанию")
    private Double defaultCommissionPercent;

}
