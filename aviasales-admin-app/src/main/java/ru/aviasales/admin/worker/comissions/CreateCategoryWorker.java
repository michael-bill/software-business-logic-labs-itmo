package ru.aviasales.admin.worker.comissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.commissions.SalesCategoryService;
import ru.aviasales.common.dto.request.SalesCategoryReq;
import ru.aviasales.common.dto.response.SalesCategoryResp;
import ru.aviasales.admin.exception.UniqueValueExistsException;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateCategoryWorker {

    private final ExternalTaskClient client;
    private final SalesCategoryService salesCategoryService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("category-create")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    String name = externalTask.getVariable("category_name");
                    String description = externalTask.getVariable("category_description");
                    Object commissionObj = externalTask.getVariable("category_comission");

                    log.info("Worker 'category-create': creating category '{}', commission: {}", name, commissionObj);

                    try {
                        if (name == null || name.trim().isEmpty()) {
                            throw new IllegalArgumentException("Название категории не заполнено");
                        }
                        if (commissionObj == null) {
                            throw new IllegalArgumentException("Значение комиссии не указано");
                        }
                        double commission;
                        try {
                            commission = Double.parseDouble(commissionObj.toString());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Комиссия должна быть числом");
                        }
                        if (commission < 0 || commission > 100) {
                            throw new IllegalArgumentException("Комиссия должна быть от 0 до 100");
                        }

                        SalesCategoryReq req = SalesCategoryReq.builder()
                                .name(name)
                                .description(description)
                                .defaultCommissionPercent(commission)
                                .build();
                        SalesCategoryResp resp = salesCategoryService.createCategory(req);
                        externalTaskService.complete(externalTask, Map.of(
                                "newCategoryCreated", true,
                                "newCategoryId", resp.getId()
                        ));
                        log.info("Worker 'category-create': category '{}' created successfully", name);
                    } catch (UniqueValueExistsException | IllegalArgumentException e) {
                        log.warn("Worker 'category-create': error: {}", e.getMessage());
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "CATEGORY_CREATE_ERROR",
                                e.getMessage(),
                                Map.of("errorNewCategoryMessage", e.getMessage())
                        );
                    } catch (Exception e) {
                        log.error("Worker 'category-create': unexpected error");
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "UNEXPECTED_ERROR",
                                "Техническая ошибка при создании категории: " + e.getMessage(),
                                Map.of("errorNewCategoryMessage", "Техническая ошибка при создании категории: " + e.getMessage())
                        );
                    }
                })
                .open();
    }
}
