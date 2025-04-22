package ru.aviasales.admin.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.aviasales.admin.service.robokassa.RobokassaHtmlService;
import ru.aviasales.admin.service.robokassa.RobokassaService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/robokassa")
@Tag(name = "Payments")
public class PaymentController {

    private final RobokassaHtmlService robokassaHtmlService;
    private final RobokassaService robokassaService;

    @Operation(summary = "Получить html страницу на оплату")
    @GetMapping("/init-payment/html")
    public ResponseEntity<?> getPaymentHtml(
            @Parameter(description = "Номер счета")
            @RequestParam(value = "inv_id")
            String invId,

            @Parameter(description = "Сумма (в руб)")
            @RequestParam(value = "amount")
            String amount,

            @Parameter(description = "Описание платежа")
            @RequestParam(value = "description")
            String description
    ) {
        try {
            String redirectUrl = "/robokassa/payment-page?inv_id=" + invId +
                    "&amount=" + amount +
                    "&description=" + URLEncoder.encode(description, StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error generating payment page");
        }
    }

    @GetMapping("/payment-page")
    public ResponseEntity<String> showPaymentPage(
            @RequestParam("inv_id") String invId,
            @RequestParam("amount") String amount,
            @RequestParam("description") String description
    ) {
        try {
            String html = robokassaHtmlService.generatePaymentHtml(invId, amount, description);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error generating payment page");
        }
    }

    @PostMapping("/result")
    public ResponseEntity<String> handleResult(
            @RequestParam("OutSum") String outSum,
            @RequestParam("InvId") String invId,
            @RequestParam("SignatureValue") String signature
    ) throws Exception {
        boolean isValid = robokassaService.validateResultSignature(outSum, invId, signature);
        log.info("Signature with OutSum {}, InvId {} and SignatureValue {} is valid: {}",
                outSum, invId, signature, isValid);
        if (isValid) {
            return ResponseEntity.ok().body("OK" + invId);
        }
        return ResponseEntity.badRequest().body("Invalid signature");
    }
}
