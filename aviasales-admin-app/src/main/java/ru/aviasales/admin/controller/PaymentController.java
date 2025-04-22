package ru.aviasales.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.aviasales.admin.service.core.ad.AdvertisementService;
import ru.aviasales.admin.service.robokassa.RobokassaHtmlService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/robokassa")
@Tag(name = "Payments (Robokassa Callbacks & Manual Init)") // Updated tag name
public class PaymentController {

    private final RobokassaHtmlService robokassaHtmlService;
    private final AdvertisementService advertisementService;

    @Operation(summary = "Инициализация платежа (Ручной/Тестовый)",
            description = "Генерирует HTML-страницу для редиректа на Robokassa. Основной способ инициации - через POST /advertisements/{id}/pay")
    @GetMapping("/init-payment")
    public ResponseEntity<String> showPaymentPageManual(
            @Parameter(description = "Номер счета (должен быть уникальным)", required = true)
            @RequestParam(value = "inv_id") String invId,

            @Parameter(description = "Сумма (в руб)", required = true)
            @RequestParam(value = "amount") String amount,

            @Parameter(description = "Описание платежа", required = true)
            @RequestParam(value = "description") String description
    ) {
        log.warn("Manual payment initiation requested for InvId: {}, Amount: {}", invId, amount);
        try {
            String html = robokassaHtmlService.generatePaymentHtml(invId, amount, description);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } catch (Exception e) {
            log.error("Error generating manual payment page: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error generating payment page");
        }
    }

    @Operation(summary = "Обработка результата платежа (Robokassa Result URL)")
    @PostMapping("/result")
    public ResponseEntity<String> handleResult(
            @Parameter(description = "Сумма заказа") @RequestParam("OutSum") String outSum,
            @Parameter(description = "Номер заказа (счета)") @RequestParam("InvId") String invId,
            @Parameter(description = "Контрольная сумма") @RequestParam("SignatureValue") String signature
    ) {
        log.info("Received Robokassa result callback for InvId: {}, OutSum: {}", invId, outSum);
        try {
            boolean success = advertisementService.processPaymentCallback(invId, outSum, signature);
            if (success) {
                return ResponseEntity.ok().body("OK" + invId);
            } else {
                log.warn("Payment callback validation failed for InvId: {}", invId);
                return ResponseEntity.badRequest().body("Payment validation failed");
            }
        } catch (Exception e) {
            log.error("Error processing Robokassa result for InvId {}: {}", invId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing payment result: " + e.getMessage());
        }
    }
}
