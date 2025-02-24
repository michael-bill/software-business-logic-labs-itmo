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
import ru.aviasales.admin.dto.request.SalesCategoryReq;
import ru.aviasales.admin.dto.response.SalesCategoryResp;
import ru.aviasales.admin.service.core.commissions.SalesCategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sales-categories")
@Tag(name = "Sales categories")
public class SalesCategoryController {

    private final SalesCategoryService salesCategoryService;

    @Operation(summary = "Получить список всех категорий")
    @PageableAsQueryParam
    @GetMapping
    public Page<SalesCategoryResp> getAllCategories(
            @Parameter(hidden = true) Pageable pageable
    ) {
        return salesCategoryService.getAllCategories(pageable);
    }

    @Operation(summary = "Получить категорию по id")
    @GetMapping("/{id}")
    public SalesCategoryResp getCategory(
            @PathVariable("id") Long id
    ) {
        return salesCategoryService.getCategory(id);
    }

    @Operation(summary = "Создать категорию")
    @PostMapping
    public SalesCategoryResp createCategory(
            @AuthenticationPrincipal User user,
            @RequestBody SalesCategoryReq salesCategoryReq
    ) {
        return salesCategoryService.createCategory(user, salesCategoryReq);
    }

    @Operation(summary = "Обновить категорию")
    @PutMapping
    public SalesCategoryResp updateCategory(
            @AuthenticationPrincipal User user,
            @RequestParam("id") Long id,
            @RequestBody SalesCategoryReq salesCategoryReq
    ) {
        return salesCategoryService.updateCategory(user, id, salesCategoryReq);
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping
    public void deleteCategory(
            @RequestParam("id") Long id
    ) {
        salesCategoryService.deleteCategory(id);
    }
}
