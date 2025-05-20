package ru.aviasales.admin.worker.comissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.commissions.SalesCategoryService;
import ru.aviasales.common.dto.response.SalesCategoryResp;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetCategoryWorker {

    private final ExternalTaskClient client;
    private final SalesCategoryService salesCategoryService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("category-get")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'category-get': processing request to get category information");

                    try {
                        Long categoryId = externalTask.getVariable("categoryId");
                        if (categoryId == null) {
                            throw new IllegalArgumentException("Category ID is required");
                        }

                        SalesCategoryResp category = salesCategoryService.getCategory(categoryId);
                        if (category == null) {
                            throw new IllegalArgumentException("Category not found with ID: " + categoryId);
                        }

                        externalTaskService.complete(externalTask, Map.of(
                            "category_id", category.getId(),
                            "category_name", category.getName(),
                            "category_description", category.getDescription() != null ? category.getDescription() : "",
                            "category_commission", category.getDefaultCommissionPercent(),
                            "category_original_commission", category.getDefaultCommissionPercent(),
                            "category_version", category.getVersion()
                        ));

                        log.info("Worker 'category-get': successfully retrieved category with ID {}", categoryId);

                    } catch (Exception e) {
                        log.error("Worker 'category-get': error while retrieving category", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при получении информации о категории: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
