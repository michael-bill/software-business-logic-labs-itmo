package ru.aviasales.admin.service.core.commissions;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.entity.SalesCategory;
import ru.aviasales.admin.dao.repository.SalesCategoryRepository;
import ru.aviasales.admin.dto.request.SalesCategoryReq;
import ru.aviasales.admin.dto.response.SalesCategoryResp;
import ru.aviasales.admin.exception.EntityNotFoundException;
import ru.aviasales.admin.exception.IllegalOperationException;
import ru.aviasales.admin.exception.OptimisticLockException;
import ru.aviasales.admin.exception.UniqueValueExistsException;

@Service
@RequiredArgsConstructor
public class SalesCategoryService {

    private final SalesCategoryRepository salesCategoryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public SalesCategoryResp createCategory(SalesCategoryReq req) {
        validateCommission(req.getDefaultCommissionPercent());

        if (salesCategoryRepository.existsByName(req.getName())) {
            throw new UniqueValueExistsException("Категория с таким именем уже существует");
        }

        SalesCategory category = SalesCategory.builder()
                .name(req.getName())
                .description(req.getDescription())
                .defaultCommissionPercent(req.getDefaultCommissionPercent())
                .build();

        return modelMapper.map(salesCategoryRepository.save(category), SalesCategoryResp.class);
    }

    @Transactional
    public SalesCategoryResp updateCategory(Long categoryId, Long version, SalesCategoryReq req) {
        validateCommission(req.getDefaultCommissionPercent());

        SalesCategory category = salesCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Категория с таким id не была найдена"));

        checkLock(version, category.getVersion());

        if (!Objects.equals(req.getName(), category.getName()) && salesCategoryRepository.existsByName(req.getName())) {
            throw new UniqueValueExistsException("Категория с таким именем уже существует");
        }

        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setDefaultCommissionPercent(req.getDefaultCommissionPercent());

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

    private void checkLock(Long userVersion, Long serverVersion) {
        if (userVersion > serverVersion || userVersion < 0) {
            throw new IllegalOperationException("Такой версии у объекта не существует");
        }
        if (userVersion < serverVersion) {
            throw new OptimisticLockException();
        }
    }
}
