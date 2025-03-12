package ru.aviasales.admin.service.core.commissions;

import java.util.Objects;

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
import ru.aviasales.admin.dto.response.SalesCategoryResp;
import ru.aviasales.admin.exception.EntityNotFoundException;
import ru.aviasales.admin.exception.UniqueValueExistsException;

@Service
@RequiredArgsConstructor
public class SalesCategoryService {

    private final SalesCategoryRepository salesCategoryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public SalesCategoryResp createCategory(User user, SalesCategoryReq req) {
        validateCommission(req.getDefaultCommissionPercent());

        if (salesCategoryRepository.existsByName(req.getName())) {
            throw new UniqueValueExistsException("Категория с таким именем уже существует");
        }

        SalesCategory category = SalesCategory.builder()
                .name(req.getName())
                .description(req.getDescription())
                .defaultCommissionPercent(req.getDefaultCommissionPercent())
                .createdBy(user)
                .updatedBy(user)
                .build();

        return modelMapper.map(salesCategoryRepository.save(category), SalesCategoryResp.class);
    }

    @Transactional
    public SalesCategoryResp updateCategory(User user, Long categoryId, SalesCategoryReq req) {
        validateCommission(req.getDefaultCommissionPercent());

        SalesCategory category = salesCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Категория не была найдена"));

        if (!Objects.equals(req.getName(), category.getName()) && salesCategoryRepository.existsByName(req.getName())) {
            throw new UniqueValueExistsException("Категория с таким именем уже существует");
        }

        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setDefaultCommissionPercent(req.getDefaultCommissionPercent());
        category.setUpdatedBy(user);

        return modelMapper.map(category, SalesCategoryResp.class);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        SalesCategory category = salesCategoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Категория не была найдена"));
        salesCategoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public SalesCategoryResp getCategory(Long categoryId) {
        return salesCategoryRepository.findById(categoryId)
                .map(x -> modelMapper.map(x, SalesCategoryResp.class))
                .orElseThrow(() -> new EntityNotFoundException("Категория не была найдена"));
    }

    @Transactional(readOnly = true)
    public Page<SalesCategoryResp> getAllCategories(Pageable pageable) {
        return salesCategoryRepository.findAll(pageable).map(x -> modelMapper.map(x, SalesCategoryResp.class));
    }

    private void validateCommission(Double commission) {
        if (commission == null || commission < 0 || commission > 100) {
            throw new IllegalArgumentException("Invalid commission value");
        }
    }
}
