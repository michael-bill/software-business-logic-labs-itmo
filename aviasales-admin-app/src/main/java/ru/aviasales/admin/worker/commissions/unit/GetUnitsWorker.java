package ru.aviasales.admin.worker.commissions.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.aviasales.admin.dao.repository.SalesCategoryRepository;
import ru.aviasales.admin.service.core.commissions.SalesUnitService;
import ru.aviasales.common.dao.entity.SalesCategory;
import ru.aviasales.common.dto.response.SalesUnitResp;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetUnitsWorker {

    private final ExternalTaskClient client;
    private final SalesUnitService salesUnitService;
    private final ObjectMapper objectMapper;
    private final SalesCategoryRepository salesCategoryRepository;

    @PostConstruct
    public void subscribe() {
        client.subscribe("unit-get-list")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'unit-get-list': processing request to get all sales units");

                    try {
                        Long categoryId = externalTask.getVariable("category_id");
                        List<SalesUnitResp> units;
                        
                        if (categoryId != null) {
                            SalesCategory category = salesCategoryRepository.findById(categoryId).get();
                            units = salesUnitService.getAllUnitsByCategory(category, PageRequest.of(0, Integer.MAX_VALUE))
                                    .getContent();
                        } else {
                            units = salesUnitService.getAllUnits(PageRequest.of(0, Integer.MAX_VALUE))
                                    .getContent();
                        }

                        List<Map<String, Object>> unitMaps = units.stream()
                                .map(unit -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("id", unit.getId());
                                    map.put("name", unit.getName());
                                    map.put("description", unit.getDescription() != null ? unit.getDescription() : "");
                                    map.put("commission", unit.getActualCommission() + "%");
                                    return map;
                                })
                                .collect(Collectors.toList());

                        externalTaskService.complete(externalTask,
                            Map.of("unitList", objectMapper.writeValueAsString(unitMaps)));

                        log.info("Worker 'unit-get-list': successfully retrieved {} units", units.size());

                    } catch (Exception e) {
                        log.error("Worker 'unit-get-list': error while retrieving units", e);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "DATABASE_ERROR",
                                "Произошла ошибка при получении списка единиц продаж: " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
