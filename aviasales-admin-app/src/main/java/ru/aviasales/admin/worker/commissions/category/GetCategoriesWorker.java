package ru.aviasales.admin.worker.commissions.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.commissions.SalesCategoryService;
import ru.aviasales.common.dto.response.SalesCategoryResp;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetCategoriesWorker {

    private final ExternalTaskClient client;
    private final SalesCategoryService salesCategoryService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        client.subscribe("category-get-list")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'category-get-list': processing request to get all sales categories");

                    try {
                        List<SalesCategoryResp> categories = salesCategoryService.getAllCategories(PageRequest.of(0, Integer.MAX_VALUE))
                                .getContent();

                        List<Map<String, Object>> categoryMaps = categories.stream()
                                .map(category -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("id", category.getId());
                                    map.put("name", category.getName());
                                    map.put("description", category.getDescription() != null ? category.getDescription() : "");
                                    map.put("commission", category.getDefaultCommissionPercent() + "%");
                                    return map;
                                })
                                .collect(Collectors.toList());

                        externalTaskService.complete(externalTask,
                            Map.of("categoryList", objectMapper.writeValueAsString(categoryMaps)));

                        log.info("Worker 'category-get-list': successfully retrieved {} categories", categories.size());

                    } catch (Exception e) {
                        log.error("Worker 'category-get-list': error while retrieving categories", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при получении списка категорий: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
