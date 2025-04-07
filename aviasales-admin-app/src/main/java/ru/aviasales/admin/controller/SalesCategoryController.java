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
import ru.aviasales.admin.dto.request.SalesCategoryReq;
import ru.aviasales.admin.dto.response.SalesCategoryResp;
import ru.aviasales.admin.service.core.commissions.SalesCategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sales/categories")
@Tag(name = "Sales categories")
@PreAuthorize("hasRole('COMMISSIONS')")
public class SalesCategoryController {

    private final SalesCategoryService salesCategoryService;

    @Operation(summary = "Получить список всех категорий")
    @PageableAsQueryParam
    @GetMapping
    public Page<SalesCategoryResp> getAllCategories(
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
        return salesCategoryService.getAllCategories(pageable);
    }

    @Operation(summary = "Получить категорию по id")
    @GetMapping("/{id}")
    public SalesCategoryResp getCategory(
            @Parameter(description = "Идентификатор категории")
            @PathVariable("id")
            Long id
    ) {
        return salesCategoryService.getCategory(id);
    }

    @Operation(summary = "Создать категорию")
    @PostMapping
    public SalesCategoryResp createCategory(
            @RequestBody SalesCategoryReq salesCategoryReq
    ) {
        return salesCategoryService.createCategory(salesCategoryReq);
    }

    @Operation(summary = "Обновить категорию")
    @PutMapping("/{id}")
    public SalesCategoryResp updateCategory(
            @Parameter(description = "Идентификатор категории")
            @PathVariable("id")
            Long id,

            @RequestBody SalesCategoryReq salesCategoryReq
    ) {
        return salesCategoryService.updateCategory(id, salesCategoryReq);
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping("/{id}")
    public void deleteCategory(
            @Parameter(description = "Идентификатор категории")
            @PathVariable("id")
            Long id
    ) {
        salesCategoryService.deleteCategory(id);
    }
}
