package ru.aviasales.admin.worker.commissions.unit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.commissions.SalesUnitService;
import ru.aviasales.common.dto.request.SalesUnitReq;
import ru.aviasales.common.dto.response.SalesUnitResp;
import ru.aviasales.admin.exception.UniqueValueExistsException;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateUnitWorker {

    private final ExternalTaskClient client;
    private final SalesUnitService salesUnitService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("unit-create")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    String name = externalTask.getVariable("unit_name");
                    String description = externalTask.getVariable("unit_description");
                    Long categoryId = externalTask.getVariable("category_id");
                    Object commissionObj = externalTask.getVariable("unit_commission");
                    boolean isDefaultCommission = externalTask.getVariable("unit_is_default_commission");

                    log.info("Worker 'unit-create': creating unit '{}', category: {}, commission: {}", 
                            name, categoryId, commissionObj);

                    try {
                        if (name == null || name.trim().isEmpty()) {
                            throw new IllegalArgumentException("Название единицы продаж не заполнено");
                        }
                        if (commissionObj == null) {
                            throw new IllegalArgumentException("Значение комиссии не указано");
                        }

                        long commission;
                        try {
                            commission = Long.parseLong(commissionObj.toString());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Комиссия должна быть числом");
                        }
                        if (commission < 0 || commission > 100) {
                            throw new IllegalArgumentException("Комиссия должна быть от 0 до 100");
                        }

                        SalesUnitReq req = SalesUnitReq.builder()
                                .name(name)
                                .description(description)
                                .categoryId(categoryId)
                                .commissionPercent(commission)
                                .build();
                        SalesUnitResp resp = salesUnitService.createSalesUnit(req);
                        if(isDefaultCommission) {
                            salesUnitService.resetToDefaultCommission(resp.getId(), resp.getVersion());
                        }
                        externalTaskService.complete(externalTask, Map.of(
                                "newUnitCreated", true,
                                "newUnitId", resp.getId(),
                                "createElement", false
                        ));
                        log.info("Worker 'unit-create': unit '{}' created successfully", name);
                    } catch (UniqueValueExistsException | IllegalArgumentException e) {
                        log.warn("Worker 'unit-create': error: {}", e.getMessage());
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "UNIT_CREATE_ERROR",
                                e.getMessage(),
                                Map.of("errorNewUnitMessage", e.getMessage())
                        );
                    } catch (Exception e) {
                        log.error("Worker 'unit-create': unexpected error");
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "UNEXPECTED_ERROR",
                                "Техническая ошибка при создании единицы продаж: " + e.getMessage(),
                                Map.of("errorNewUnitMessage", "Техническая ошибка при создании единицы продаж: " + e.getMessage())
                        );
                    }
                })
                .open();
    }
}
