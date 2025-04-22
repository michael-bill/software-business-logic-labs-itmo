package ru.aviasales.admin.controller;

import java.util.List;

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
import ru.aviasales.common.dto.response.MessageResp;
import ru.aviasales.common.dto.response.TaskStatusResp;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advertisements")
@Tag(name = "Advertisements")
public class AdvertisementController {

    private final KafkaProducerService kafkaProducerService;
    private final AdvertisementService advertisementService;
    private final TaskStatusService taskStatusService;

    @Operation(summary = "Получить список всех рекламных объявлений")
    @PageableAsQueryParam
    @GetMapping
    @PreAuthorize("hasAuthority('READ_ADVERTISEMENTS')")
    public Page<AdvertisementResp> getAllAdvertisements(
                        @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Номер страницы не может быть меньше 0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Размер страницы не может быть меньше 1")
            @Max(value = 100, message = "Размер страницы не может превышать 100")
            int size,

            @RequestParam(required = false)
            List<String> sort,

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

    @Operation(summary = "Создать рекламное объявление")
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ADVERTISEMENT')")
    public ResponseEntity<CreateTaskResp> createAdvertisement(
            @RequestBody AdvertisementReq req
    ) {
        CreateTaskResp response = taskStatusService.initiateAdvertisementCreation(req);
        return ResponseEntity.accepted().body(response);
    }

    @Operation(summary = "Получить статус задачи создания рекламного объявления")
    @GetMapping("/tasks/{taskId}/status")
    @PreAuthorize("hasAuthority('READ_ADVERTISEMENTS')")
    public TaskStatusResp getAdvertisementTaskStatus(
            @Parameter(description = "Идентификатор задачи (UUID)")
            @PathVariable("taskId") String taskId
    ) {
        return taskStatusService.getTaskStatus(taskId);
    }
}
