package ru.aviasales.admin.worker.comissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.commissions.SalesUnitService;
import ru.aviasales.common.dto.request.SalesUnitReq;
import ru.aviasales.common.dto.response.SalesUnitResp;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SetUnitCommissionWorker {

    private final ExternalTaskClient client;
    private final SalesUnitService salesUnitService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("unit-set-comission")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'unit-set-comission': processing request to set unit commission");

                    try {
                        Long unitId = externalTask.getVariable("unit_id");
                        Long version = externalTask.getVariable("unit_version");
                        Object commissionObj = externalTask.getVariable("unit_commission");

                        if (unitId == null) {
                            throw new IllegalArgumentException("Unit ID is required");
                        }
                        if (version == null) {
                            throw new IllegalArgumentException("Unit version is required");
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
                            throw new IllegalArgumentException("Commission must be between 0 and 100");
                        }

                        SalesUnitResp unit = salesUnitService.getUnitById(unitId);
                        if (unit == null) {
                            throw new IllegalArgumentException("Unit not found with ID: " + unitId);
                        }

                        SalesUnitReq req = SalesUnitReq.builder()
                                .name(unit.getName())
                                .description(unit.getDescription())
                                .categoryId(unit.getCategory().getId())
                                .commissionPercent(commission)
                                .build();

                        salesUnitService.updateSalesUnit(unitId, version, req);
                        externalTaskService.complete(externalTask);

                        log.info("Worker 'unit-set-comission': successfully set commission for unit ID {}", unitId);

                    } catch (IllegalArgumentException e) {
                        log.warn("Worker 'unit-set-comission': validation error: {}", e.getMessage());
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "UNIT_COMISSION_ERROR",
                                e.getMessage(),
                                Map.of("errorUnitComission", e.getMessage())
                        );
                    } catch (Exception e) {
                        log.error("Worker 'unit-set-comission': unexpected error", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при установке комиссии для единицы продаж: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
