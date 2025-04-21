package ru.aviasales.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementReq {

    @NotNull(message = "Название объявления не может быть пустым")
    @Schema(description = "Название объявления")
    private String title;

    @NotNull(message = "Название компании не может быть пустым")
    @Schema(description = "Название компании")
    private String companyName;

    @NotNull(message = "Описание объявления не может быть пустым")
    @Schema(description = "Описание объявления")
    private String description;

    @NotNull(message = "Тип рекламы не может быть пустым")
    @Schema(description = "Тип рекламы")
    private Long adTypeId;

    @Schema(description = "Сегменты пользователей")
    private Set<Long> targetSegmentIds;

    @NotNull(message = "Срок действия рекламы не может быть пустым")
    @Schema(description = "Срок действия рекламы")
    private LocalDateTime deadline;
}
