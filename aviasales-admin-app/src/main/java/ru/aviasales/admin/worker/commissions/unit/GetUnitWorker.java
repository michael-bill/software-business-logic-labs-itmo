package ru.aviasales.admin.worker.commissions.unit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.service.core.commissions.SalesUnitService;
import ru.aviasales.common.dto.response.SalesUnitResp;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetUnitWorker {

    private final ExternalTaskClient client;
    private final SalesUnitService salesUnitService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("unit-get")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'unit-get': processing request to get unit information");

                    try {
                        Long unitId = externalTask.getVariable("unitId");
                        if (unitId == null) {
                            throw new IllegalArgumentException("Unit ID is required");
                        }

                        SalesUnitResp unit = salesUnitService.getUnitById(unitId);
                        if (unit == null) {
                            throw new IllegalArgumentException("Unit not found with ID: " + unitId);
                        }

                        externalTaskService.complete(externalTask, Map.of(
                            "unit_id", unit.getId(),
                            "unit_name", unit.getName(),
                            "unit_description", unit.getDescription() != null ? unit.getDescription() : "",
                            "unit_category_id", unit.getCategory().getId(),
                            "unit_category_name", unit.getCategory().getName(),
                            "unit_commission", unit.getActualCommission(),
                            "unit_original_commission", unit.getActualCommission(),
                            "unit_version", unit.getVersion()
                        ));

                        log.info("Worker 'unit-get': successfully retrieved unit with ID {}", unitId);

                    } catch (Exception e) {
                        log.error("Worker 'unit-get': error while retrieving unit");
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при получении информации о единице продаж: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
