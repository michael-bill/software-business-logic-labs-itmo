package ru.aviasales.admin.controller;

import java.net.URI;
import java.util.List;
import java.util.Map; // For returning the payment URL

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.aviasales.admin.configuration.PageableAsQueryParam;
import ru.aviasales.admin.service.core.TaskStatusService;
import ru.aviasales.admin.service.messaging.KafkaProducerService;
import ru.aviasales.common.dto.request.AdvertisementReq;
import ru.aviasales.common.dto.response.AdvertisementResp;
import ru.aviasales.admin.service.core.ad.AdvertisementService;
import ru.aviasales.common.dto.response.CreateTaskResp;
import ru.aviasales.common.dto.response.TaskStatusResp;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advertisements")
@Tag(name = "Advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;
    private final TaskStatusService taskStatusService;

    @Operation(summary = "Получить список всех рекламных объявлений")
    @PageableAsQueryParam
    @GetMapping
    @PreAuthorize("hasAuthority('READ_ADVERTISEMENTS')")
    public Page<AdvertisementResp> getAllAdvertisements(
            @Parameter(hidden = true) Pageable pageable
    ) {
        return advertisementService.getAllAdvertisements(pageable);
    }

    @Operation(summary = "Получить рекламное объявление по id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_ADVERTISEMENTS')")
    public AdvertisementResp getAdvertisementById(
            @PathVariable("id") Long id
    ) {
        return advertisementService.getAdvertisementById(id);
    }

    @Operation(summary = "Создать запрос на создание рекламного объявления (асинхронно)")
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ADVERTISEMENT')")
    public ResponseEntity<CreateTaskResp> createAdvertisement(
            @RequestBody AdvertisementReq req
    ) {
        CreateTaskResp response = taskStatusService.initiateAdvertisementCreation(req);
        return ResponseEntity.accepted().body(response);
    }

    @Operation(summary = "Получить статус задачи создания/обработки рекламного объявления")
    @GetMapping("/tasks/{taskId}/status")
    @PreAuthorize("hasAuthority('READ_ADVERTISEMENTS')") // Or a more general task read permission
    public TaskStatusResp getAdvertisementTaskStatus(
            @Parameter(description = "Идентификатор задачи (UUID)")
            @PathVariable("taskId") String taskId
    ) {
        return taskStatusService.getTaskStatus(taskId);
    }

    @Operation(summary = "Получить список неоплаченных рекламных объявлений")
    @PageableAsQueryParam
    @GetMapping("/unpaid")
    @PreAuthorize("hasAuthority('READ_ADVERTISEMENTS')")
    public Page<AdvertisementResp> getUnpaidAdvertisements(
            @Parameter(hidden = true) Pageable pageable
    ) {
        return advertisementService.findUnpaidAdvertisements(pageable);
    }

    @Operation(summary = "Инициировать оплату для рекламного объявления",
            description = "Возвращает URL для редиректа пользователя на страницу оплаты Robokassa.")
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('CREATE_ADVERTISEMENT')") // Or a specific PAY_ADVERTISEMENT permission
    public ResponseEntity<Map<String, String>> initiatePayment(
            @Parameter(description = "ID рекламного объявления для оплаты")
            @PathVariable("id") Long advertisementId
    ) {
        String paymentUrl = advertisementService.initiatePaymentForAdvertisement(advertisementId);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }
}
