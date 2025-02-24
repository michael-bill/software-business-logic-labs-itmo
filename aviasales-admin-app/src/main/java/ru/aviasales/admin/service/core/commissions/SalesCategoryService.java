package ru.aviasales.admin.service.core.commissions;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.entity.SalesCategory;
import ru.aviasales.admin.dao.entity.User;
import ru.aviasales.admin.dao.repository.SalesCategoryRepository;
import ru.aviasales.admin.dto.request.SalesCategoryReq;
import ru.aviasales.admin.dto.response.SalesCategoryRes;
import ru.aviasales.admin.exception.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class SalesCategoryService {

    private final SalesCategoryRepository salesCategoryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public SalesCategoryRes createCategory(User user, SalesCategoryReq req) {
        validateCommission(req.getDefaultCommissionPercent());

        var now = LocalDateTime.now();

        SalesCategory category = SalesCategory.builder()
                .name(req.getName())
                .description(req.getDescription())
                .defaultCommissionPercent(req.getDefaultCommissionPercent())
                .createdAt(now)
                .updatedAt(now)
                .createdBy(user)
                .updatedBy(user)
                .build();

        return modelMapper.map(salesCategoryRepository.save(category), SalesCategoryRes.class);
    }

    @Transactional
    public SalesCategoryRes updateCategory(User user, Long categoryId, SalesCategoryReq req) {
        validateCommission(req.getDefaultCommissionPercent());

        SalesCategory category = salesCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Категория не была найдена"));

        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setDefaultCommissionPercent(req.getDefaultCommissionPercent());
        category.setUpdatedBy(user);
        category.setUpdatedAt(LocalDateTime.now());

        return modelMapper.map(category, SalesCategoryRes.class);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        salesCategoryRepository.deleteById(categoryId);
    }

    @Transactional(readOnly = true)
    public SalesCategoryRes getCategory(Long categoryId) {
        return salesCategoryRepository.findById(categoryId)
                .map(x -> modelMapper.map(x, SalesCategoryRes.class))
                .orElseThrow(() -> new EntityNotFoundException("Категория не была найдена"));
    }

    @Transactional(readOnly = true)
    public Page<SalesCategoryRes> getAllCategories(Pageable pageable) {
        return salesCategoryRepository.findAll(pageable).map(x -> modelMapper.map(x, SalesCategoryRes.class));
    }

    private void validateCommission(Double commission) {
        if (commission == null || commission < 0 || commission > 100) {
            throw new IllegalArgumentException("Invalid commission value");
        }
    }
}
