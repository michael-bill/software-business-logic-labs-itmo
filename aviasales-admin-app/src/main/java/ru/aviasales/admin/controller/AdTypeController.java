package ru.aviasales.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.aviasales.admin.configuration.PageableAsQueryParam;
import ru.aviasales.admin.dto.response.AdTypeResp;
import ru.aviasales.admin.service.core.ad.AdTypeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advertisements")
@Tag(name = "Advertisements")
public class AdTypeController {

    private final AdTypeService adTypeService;

    @Operation(summary = "Получить список всех типов объявлений")
    @PageableAsQueryParam
    @GetMapping("/types")
    public Page<AdTypeResp> getAllAdTypes(
            @Parameter(hidden = true) Pageable pageable
    ) {
        return adTypeService.getAllAdTypes(pageable);
    }
}
