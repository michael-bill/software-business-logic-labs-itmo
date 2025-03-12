package ru.aviasales.admin.service.core.commissions;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.entity.SalesCategory;
import ru.aviasales.admin.dao.entity.SalesUnit;
import ru.aviasales.admin.dao.entity.User;
import ru.aviasales.admin.dao.repository.SalesCategoryRepository;
import ru.aviasales.admin.dao.repository.SalesUnitRepository;
import ru.aviasales.admin.dto.request.SalesUnitReq;
import ru.aviasales.admin.dto.response.SalesUnitResp;
import ru.aviasales.admin.exception.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class SalesUnitService {

    private final SalesUnitRepository salesUnitRepository;
    private final SalesCategoryRepository salesCategoryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public SalesUnitResp createSalesUnit(User user, SalesUnitReq req) {
        if (req.getCommissionPercent() != null) {
            validateCommission(req.getCommissionPercent());
        }

        SalesCategory category = salesCategoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория c id %d не была найдена".formatted(req.getCategoryId())
                ));

        SalesUnit unit = SalesUnit.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(category)
                .customCommissionPercent(req.getCommissionPercent())
                .isCustomCommission(req.getCommissionPercent() != null)
                .createdBy(user)
                .updatedBy(user)
                .build();

        return modelMapper.map(salesUnitRepository.save(unit), SalesUnitResp.class);
    }

    @Transactional
    public SalesUnitResp updateSalesUnit(User user, Long unitId, SalesUnitReq req) {
        if (req.getCommissionPercent() != null) {
            validateCommission(req.getCommissionPercent());
        }

        SalesUnit unit = salesUnitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Единица продажи с id %d не была найдена".formatted(unitId)
                ));

        SalesCategory category = salesCategoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория c id %d не была найдена".formatted(req.getCategoryId())
                ));

        unit.setName(req.getName());
        unit.setDescription(req.getDescription());
        unit.setCategory(category);
        unit.setCustomCommissionPercent(req.getCommissionPercent());
        unit.setIsCustomCommission(req.getCommissionPercent() != null);
        unit.setUpdatedBy(user);

        return modelMapper.map(unit, SalesUnitResp.class);
    }

    @Transactional
    public SalesUnitResp resetToDefaultCommission(User user, Long unitId) {

        SalesUnit unit = salesUnitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Единица продажи с id %d не была найдена".formatted(unitId)
                ));

        unit.setCustomCommissionPercent(null);
        unit.setIsCustomCommission(false);
        unit.setUpdatedBy(user);

        return modelMapper.map(unit, SalesUnitResp.class);
    }

    @Transactional
    public void deleteSalesUnit(Long unitId) {
        SalesUnit salesUnit = salesUnitRepository.findById(unitId).orElseThrow(() -> new EntityNotFoundException("Единица продажи с id %d не была найдена".formatted(unitId)));
        salesUnitRepository.delete(salesUnit);
    }

    @Transactional(readOnly = true)
    public SalesUnitResp getUnitById(Long unitId) {
        return salesUnitRepository.findById(unitId)
                .map(x -> modelMapper.map(x, SalesUnitResp.class))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Единица продажи с id %d не была найдена".formatted(unitId)
                ));
    }

    @Transactional(readOnly = true)
    public Page<SalesUnitResp> getAllUnits(Pageable pageable) {
        return salesUnitRepository.findAll(pageable).map(x -> modelMapper.map(x, SalesUnitResp.class));
    }

    private void validateCommission(Double commission) {
        if (commission == null || commission < 0 || commission > 100) {
            throw new IllegalArgumentException("Invalid commission value");
        }
    }

}
