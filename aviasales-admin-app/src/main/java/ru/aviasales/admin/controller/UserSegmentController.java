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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.aviasales.admin.configuration.PageableAsQueryParam;
import ru.aviasales.admin.dto.response.UserSegmentResp;
import ru.aviasales.admin.service.core.ad.UserSegmentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advertisements")
@Tag(name = "Advertisements")
public class UserSegmentController {

    private final UserSegmentService userSegmentService;

    @Operation(summary = "Получить список всех сегментов пользователей")
    @PageableAsQueryParam
    @GetMapping("/segments")
    @PreAuthorize("hasAuthority('READ_USER_SEGMENTS')")
    public Page<UserSegmentResp> getAllUserSegments(
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
        return userSegmentService.getAllUserSegments(pageable);
    }
}
