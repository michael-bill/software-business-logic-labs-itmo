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
public class GetCategoryVersionWorker {

    private final ExternalTaskClient client;
    private final SalesCategoryService salesCategoryService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("category-get-version")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'category-get-version': processing request to get category version");

                    try {
                        Long categoryId = externalTask.getVariable("category_id");
                        if (categoryId == null) {
                            throw new IllegalArgumentException("Category ID is required");
                        }

                        SalesCategoryResp category = salesCategoryService.getCategory(categoryId);
                        if (category == null) {
                            throw new IllegalArgumentException("Category not found with ID: " + categoryId);
                        }

                        Long originalVersion = externalTask.getVariable("category_version");
                        boolean versionChanged = !category.getVersion().equals(originalVersion);

                        externalTaskService.complete(externalTask, Map.of(
                            "version_changed", versionChanged
                        ));

                        log.info("Worker 'category-get-version': successfully checked version for category ID {}", categoryId);

                    } catch (Exception e) {
                        log.error("Worker 'category-get-version': error while checking category version", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при проверке версии категории: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
