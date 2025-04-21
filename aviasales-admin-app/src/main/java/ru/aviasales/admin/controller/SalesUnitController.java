package ru.aviasales.admin.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.aviasales.admin.configuration.PageableAsQueryParam;
import ru.aviasales.common.dto.request.SalesUnitReq;
import ru.aviasales.common.dto.response.SalesUnitResp;
import ru.aviasales.admin.service.core.commissions.SalesUnitService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sales/units")
@Tag(name = "Sales units")
public class SalesUnitController {

    private final SalesUnitService salesUnitService;

    @Operation(summary = "Получить список всех единиц продаж")
    @PageableAsQueryParam
    @GetMapping
    @PreAuthorize("hasAuthority('READ_SALES_UNITS')")
    public Page<SalesUnitResp> getAllUnits(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Номер страницы не может быть меньше 0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Размер страницы не может быть меньше 1")
            @Max(value = 100, message = "Размер страницы не может превышать 100")
            int size,

            @RequestParam(required = false)
            List<String> sort,

            @Parameter(hidden = true) Pageable pageable
    ) {
        return salesUnitService.getAllUnits(pageable);
    }

    @Operation(summary = "Получить единицу продажи по id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_SALES_UNITS')")
    public SalesUnitResp getUnitById(
            @Parameter(description = "Идентификатор единицы продажи")
            @PathVariable("id")
            Long id
    ) {
        return salesUnitService.getUnitById(id);
    }

    @Operation(summary = "Создать единицу продажи")
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_SALES_UNIT')")
    public SalesUnitResp createUnit(
            @RequestBody SalesUnitReq req
    ) {
        return salesUnitService.createSalesUnit(req);
    }

    @Operation(summary = "Обновить единицу продажи")
    @PutMapping("/{id}/versions/{version}")
    @PreAuthorize("hasAuthority('UPDATE_SALES_UNIT')")
    public SalesUnitResp updateUnit(
            @Parameter(description = "Идентификатор единицы продажи")
            @PathVariable("id")
            Long id,

            @Parameter(description = "Версия единицы (для оптимистичной блокировки)")
            @PathVariable("version")
            Long version,

            @RequestBody SalesUnitReq req
    ) {
        return salesUnitService.updateSalesUnit(id, version, req);
    }

    @Operation(summary = "Удалить единицу продажи")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_SALES_UNIT')")
    public void deleteUnit(
            @PathVariable("id") Long id
    ) {
        salesUnitService.deleteSalesUnit(id);
    }

    @Operation(summary = "Сбросить комиссию у единицы продажи")
    @PutMapping("/{id}/versions/{version}/reset-commission")
    @PreAuthorize("hasAuthority('UPDATE_SALES_UNIT')")
    public SalesUnitResp resetCommission(
            @Parameter(description = "Идентификатор единицы продажи")
            @PathVariable("id")
            Long id,

            @Parameter(description = "Версия единицы (для оптимистичной блокировки)")
            @PathVariable("version")
            Long version
    ) {
        return salesUnitService.resetToDefaultCommission(id, version);
    }

}
