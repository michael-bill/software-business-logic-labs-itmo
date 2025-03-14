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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.aviasales.admin.configuration.PageableAsQueryParam;
import ru.aviasales.admin.dao.entity.User;
import ru.aviasales.admin.dto.request.AdvertisementReq;
import ru.aviasales.admin.dto.response.AdvertisementResp;
import ru.aviasales.admin.service.core.ad.AdvertisementService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advertisements")
@Tag(name = "Advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "Получить список всех рекламных объявлений")
    @PageableAsQueryParam
    @GetMapping
    public Page<AdvertisementResp> getAllAdvertisements(
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
        return advertisementService.getAllAdvertisements(pageable);
    }

    @Operation(summary = "Получить рекламное объявление по id")
    @GetMapping("/{id}")
    public AdvertisementResp getAdvertisementById(
            @PathVariable("id") Long id
    ) {
        return advertisementService.getAdvertisementById(id);
    }

    @Operation(summary = "Создать рекламное объявление")
    @PostMapping
    public AdvertisementResp createAdvertisement(
            @AuthenticationPrincipal User user,
            @RequestBody AdvertisementReq req
    ) {
        return advertisementService.createAdvertisement(user, req);
    }
}
