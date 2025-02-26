package ru.aviasales.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import ru.aviasales.admin.dao.entity.User;
import ru.aviasales.admin.dto.request.SalesUnitReq;
import ru.aviasales.admin.dto.response.SalesUnitResp;
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
    public Page<SalesUnitResp> getAllCategories(
            @Parameter(hidden = true) Pageable pageable
    ) {
        return salesUnitService.getAllUnits(pageable);
    }

    @Operation(summary = "Получить единицу продажи по id")
    @GetMapping("/{id}")
    public SalesUnitResp getCategoryById(
            @Parameter(description = "Идентификатор единицы продажи")
            @PathVariable("id")
            Long id
    ) {
        return salesUnitService.getUnitById(id);
    }

    @Operation(summary = "Создать единицу продажи")
    @PostMapping
    public SalesUnitResp createCategory(
            @AuthenticationPrincipal User user,
            @RequestBody SalesUnitReq req
    ) {
        return salesUnitService.createSalesUnit(user, req);
    }

    @Operation(summary = "Обновить единицу продажи")
    @PutMapping("/{id}")
    public SalesUnitResp updateCategory(
            @AuthenticationPrincipal User user,

            @Parameter(description = "Идентификатор единицы продажи")
            @PathVariable("id")
            Long id,

            @RequestBody SalesUnitReq req
    ) {
        return salesUnitService.updateSalesUnit(user, id, req);
    }

    @Operation(summary = "Удалить единицу продажи")
    @DeleteMapping("/{id}")
    public void deleteCategory(
            @PathVariable("id") Long id
    ) {
        salesUnitService.deleteSalesUnit(id);
    }

    @Operation(summary = "Сбросить комиссию у единицы продажи")
    @PutMapping("/{id}/reset-commission")
    public SalesUnitResp resetCommission(
            @AuthenticationPrincipal User user,

            @Parameter(description = "Идентификатор единицы продажи")
            @PathVariable("id")
            Long id
    ) {
        return salesUnitService.resetToDefaultCommission(user, id);
    }

}
