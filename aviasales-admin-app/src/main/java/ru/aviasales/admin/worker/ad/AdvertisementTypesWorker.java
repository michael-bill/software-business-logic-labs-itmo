package ru.aviasales.admin.worker.ad;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.ad.AdTypeService;
import ru.aviasales.common.dto.response.AdTypeResp;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdvertisementTypesWorker {

    private final ExternalTaskClient client;
    private final AdTypeService adTypeService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        client.subscribe("advertisement-types-get-list")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'advertisement-types-get-list': processing request to get all advertisement types");

                    try {
                        List<AdTypeResp> adTypes = adTypeService.getAllAdTypes(PageRequest.of(0, Integer.MAX_VALUE))
                                .getContent();

                        List<Map<String, Object>> adTypeMaps = adTypes.stream()
                                .map(adType -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("id", adType.getId());
                                    map.put("name", adType.getName());
                                    map.put("segmentation", adType.getSupportsSegmentation() ? "Да" : "Нет");
                                    map.put("active", adType.getActive() ? "Да" : "Нет");
                                    return map;
                                })
                                .collect(Collectors.toList());

                        Map<String, Object> variables = new HashMap<>();
                        variables.put("showList", adTypeMaps);
                        externalTaskService.complete(externalTask, variables);

                        log.info("Worker 'advertisement-types-get-list': successfully retrieved {} advertisement types", adTypes.size());

                    } catch (Exception e) {
                        log.error("Worker 'advertisement-types-get-list': error while retrieving advertisement types", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при получении списка типов рекламы: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
