package ru.aviasales.admin.worker.ad;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.TaskStatusService;
import ru.aviasales.common.dto.request.AdvertisementReq;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdvertisementCreateWorker {

    private final ExternalTaskClient client;
    private final TaskStatusService taskStatusService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        client.subscribe("advertisement-create")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    String title = externalTask.getVariable("ad_title");
                    String companyName = externalTask.getVariable("ad_company_name");
                    String description = externalTask.getVariable("ad_description");
                    Long adTypeId = externalTask.getVariable("ad_type_id");
                    String targetSegmentIdsStr = externalTask.getVariable("taglist_segments");

                    log.info("Worker 'advertisement-create': creating advertisement with title: {}", title);

                    try {
                        if (title == null || title.trim().isEmpty()) {
                            throw new IllegalArgumentException("Название объявления не заполнено");
                        }
                        if (companyName == null || companyName.trim().isEmpty()) {
                            throw new IllegalArgumentException("Название компании не заполнено");
                        }
                        if (description == null || description.trim().isEmpty()) {
                            throw new IllegalArgumentException("Описание объявления не заполнено");
                        }
                        if (adTypeId == null) {
                            throw new IllegalArgumentException("Тип рекламы не указан");
                        }

                        Set<Long> targetSegmentIds = null;
                        if (targetSegmentIdsStr != null && !targetSegmentIdsStr.isEmpty()) {
                            targetSegmentIds = objectMapper.readValue(targetSegmentIdsStr, new TypeReference<>() {});
                        }

                        AdvertisementReq req = AdvertisementReq.builder()
                                .title(title)
                                .companyName(companyName)
                                .description(description)
                                .adTypeId(adTypeId)
                                .targetSegmentIds(targetSegmentIds)
                                .deadline(LocalDateTime.now().plusMonths(1))
                                .build();

                        taskStatusService.initiateAdvertisementCreation(req);
                        externalTaskService.complete(externalTask);

                        log.info("Worker 'advertisement-create': advertisement creation initiated successfully");

                    } catch (IllegalArgumentException e) {
                        log.warn("Worker 'advertisement-create': validation error: {}", e.getMessage());
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "VALIDATION_ERROR",
                                e.getMessage(),
                                null
                        );
                    } catch (Exception e) {
                        log.error("Worker 'advertisement-create': unexpected error", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "UNEXPECTED_ERROR",
                                "Техническая ошибка при создании рекламы: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
