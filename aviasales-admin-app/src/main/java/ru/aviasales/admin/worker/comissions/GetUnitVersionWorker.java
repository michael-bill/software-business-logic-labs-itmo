package ru.aviasales.admin.worker.comissions;

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
public class GetUnitVersionWorker {

    private final ExternalTaskClient client;
    private final SalesUnitService salesUnitService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("unit-get-version")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'unit-get-version': processing request to get unit version");

                    try {
                        Long unitId = externalTask.getVariable("unit_id");
                        if (unitId == null) {
                            throw new IllegalArgumentException("Unit ID is required");
                        }

                        SalesUnitResp unit = salesUnitService.getUnitById(unitId);
                        if (unit == null) {
                            throw new IllegalArgumentException("Unit not found with ID: " + unitId);
                        }

                        Long originalVersion = externalTask.getVariable("unit_version");
                        boolean versionChanged = !unit.getVersion().equals(originalVersion);

                        externalTaskService.complete(externalTask, Map.of(
                            "unit_version_changed", versionChanged
                        ));

                        log.info("Worker 'unit-get-version': successfully checked version for unit ID {}", unitId);

                    } catch (Exception e) {
                        log.error("Worker 'unit-get-version': error while checking unit version", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при проверке версии единицы продаж: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
