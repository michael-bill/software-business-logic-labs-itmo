package ru.aviasales.admin.worker.comissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.exception.OptimisticLockException;
import ru.aviasales.admin.exception.UniqueValueExistsException;
import ru.aviasales.admin.service.core.commissions.SalesCategoryService;
import ru.aviasales.common.dto.request.SalesCategoryReq;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SetCategoryCommissionWorker {

    private final ExternalTaskClient client;
    private final SalesCategoryService salesCategoryService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("category-set-comission")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'category-set-comission': processing request to set category commission");

                    try {
                        Long categoryId = externalTask.getVariable("category_id");
                        Object commissionObj = externalTask.getVariable("category_commission");
                        Long version = externalTask.getVariable("category_version");

                        if (categoryId == null) {
                            throw new IllegalArgumentException("Category ID is required");
                        }
                        if (commissionObj == null) {
                            throw new IllegalArgumentException("Commission value is required");
                        }

                        long commission;
                        try {
                            commission = Long.parseLong(commissionObj.toString());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Commission must be a number");
                        }

                        if (commission < 0 || commission > 100) {
                            externalTaskService.handleBpmnError(
                                    externalTask,
                                    "CATEGORY_COMISSION_ERROR",
                                    "Значение комиссии должно быть в диапазоне от 0 до 100",
                                    Map.of("errorCategoryComission", "Значение комиссии должно быть в диапазоне от 0 до 100")
                            );
                            return;
                        }

                        SalesCategoryReq req = SalesCategoryReq.builder()
                                .name(externalTask.getVariable("category_name"))
                                .description(externalTask.getVariable("category_description"))
                                .defaultCommissionPercent(commission)
                                .build();

                        try {
                            salesCategoryService.updateCategory(categoryId, version, req);
                        } catch (OptimisticLockException e) {
                            externalTaskService.handleBpmnError(
                                    externalTask,
                                    "CATEGORY_COMISSION_LOCK",
                                    "Значение комиссии изменилось",
                                    Map.of("category_version", version)
                            );
                        }
                        log.info("Worker 'category-set-comission': successfully set commission {} for category ID {}", commission, categoryId);

                        externalTaskService.complete(externalTask);

                    } catch (IllegalArgumentException e) {
                        log.warn("Worker 'category-set-comission': validation error: {}", e.getMessage());
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "CATEGORY_COMISSION_ERROR",
                                e.getMessage(),
                                Map.of("errorCategoryComission", e.getMessage())
                        );
                    } catch (Exception e) {
                        log.error("Worker 'category-set-comission': unexpected error", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при установке комиссии: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
