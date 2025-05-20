package ru.aviasales.admin.worker.ad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.ad.AdTypeService;
import ru.aviasales.admin.service.core.ad.UserSegmentService;
import ru.aviasales.common.dto.response.AdTypeResp;
import ru.aviasales.common.dto.response.UserSegmentResp;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdvertisementCheckSegmentationWorker {

    private final ExternalTaskClient client;
    private final AdTypeService adTypeService;
    private final UserSegmentService userSegmentService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("advertisement-check-segmentation")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    Long adTypeId = externalTask.getVariable("ad_type_id");
                    log.info("Worker 'advertisement-check-segmentation': checking segmentation support for ad type ID: {}", adTypeId);

                    try {
                        if (adTypeId == null) {
                            throw new IllegalArgumentException("ID типа рекламы не указан");
                        }

                        AdTypeResp adType = adTypeService.getAllAdTypes(PageRequest.of(0, Integer.MAX_VALUE))
                                .getContent()
                                .stream()
                                .filter(type -> type.getId().equals(adTypeId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Тип рекламы с ID " + adTypeId + " не найден"));

                        boolean supportsSegmentation = adType.getSupportsSegmentation();
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("ad_supports_segmentation", supportsSegmentation);

                        if (supportsSegmentation) {
                            List<UserSegmentResp> userSegments = userSegmentService.getAllUserSegments(PageRequest.of(0, Integer.MAX_VALUE))
                                    .getContent();

                            List<Map<String, Object>> segmentOptions = userSegments.stream()
                                    .map(segment -> {
                                        Map<String, Object> option = new HashMap<>();
                                        option.put("value", segment.getId());
                                        option.put("label", segment.getName() + " (" + segment.getEstimatedAmount() + " пользователей)");
                                        return option;
                                    })
                                    .collect(Collectors.toList());

                            variables.put("userSegments", segmentOptions);
                        }

                        externalTaskService.complete(externalTask, variables);
                        log.info("Worker 'advertisement-check-segmentation': segmentation support check completed for ad type ID: {}", adTypeId);

                    } catch (IllegalArgumentException e) {
                        log.warn("Worker 'advertisement-check-segmentation': validation error: {}", e.getMessage());
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "VALIDATION_ERROR",
                                e.getMessage(),
                                null
                        );
                    } catch (Exception e) {
                        log.error("Worker 'advertisement-check-segmentation': unexpected error", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "UNEXPECTED_ERROR",
                                "Техническая ошибка при проверке поддержки сегментации: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
