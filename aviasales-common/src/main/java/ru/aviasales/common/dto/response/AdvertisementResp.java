package ru.aviasales.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Рекламное объявление")
public class AdvertisementResp {
    @Schema(description = "Идентификатор рекламного объявления")
    private Long id;
    @Schema(description = "Тип рекламы")
    private AdTypeResp adType;
    @Schema(description = "Заголовок объявления")
    private String title;
    @Schema(description = "Название компании")
    private String companyName;
    @Schema(description = "Описание задачи")
    private String description;
    @Schema(description = "Целевые сегменты пользователей")
    private Set<UserSegmentResp> targetSegments;
    @Schema(description = "Срок действия рекламы")
    private LocalDateTime deadline;
    @Schema(description = "Время создания рекламной задачи")
    private LocalDateTime createdAt;
    @Schema(description = "Статус оплаты")
    private Boolean payed;
    @Schema(description = "Идентификатор счета Robokassa")
    private String invoiceId;
    @Schema(description = "Html page для оплаты (если платеж инициирован)")
    private String paymentPage;
}
