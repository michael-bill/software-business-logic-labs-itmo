package ru.aviasales.admin.service.core.commissions;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.common.dao.entity.SalesCategory;
import ru.aviasales.common.dao.entity.SalesUnit;
import ru.aviasales.admin.dao.repository.SalesCategoryRepository;
import ru.aviasales.admin.dao.repository.SalesUnitRepository;
import ru.aviasales.common.dto.request.SalesUnitReq;
import ru.aviasales.common.dto.response.SalesUnitResp;
import ru.aviasales.admin.exception.EntityNotFoundException;
import ru.aviasales.admin.exception.IllegalOperationException;
import ru.aviasales.admin.exception.OptimisticLockException;
import ru.aviasales.admin.exception.UniqueValueExistsException;

@Service
@RequiredArgsConstructor
public class SalesUnitService {

    private final SalesUnitRepository salesUnitRepository;
    private final SalesCategoryRepository salesCategoryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public SalesUnitResp createSalesUnit(SalesUnitReq req) {
        if (req.getCommissionPercent() != null) {
            validateCommission(Double.valueOf(req.getCommissionPercent()));
        }

        SalesCategory category = salesCategoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория c id %d не была найдена".formatted(req.getCategoryId())
                ));

        if (salesUnitRepository.existsByName(req.getName())) {
            throw new UniqueValueExistsException("Единица продажи с таким именем уже существует");
        }

        SalesUnit unit = SalesUnit.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(category)
                .customCommissionPercent(Double.valueOf(req.getCommissionPercent()))
                .isCustomCommission(req.getCommissionPercent() != null)
                .build();

        return modelMapper.map(salesUnitRepository.save(unit), SalesUnitResp.class);
    }

    @Transactional
    public SalesUnitResp updateSalesUnit(Long unitId, Long version, SalesUnitReq req) {
        if (req.getCommissionPercent() != null) {
            validateCommission(Double.valueOf(req.getCommissionPercent()));
        }

        SalesUnit unit = salesUnitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Единица продажи с id %d не была найдена".formatted(unitId)
                ));

        checkLock(version, unit.getVersion());

        SalesCategory category = salesCategoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория c id %d не была найдена".formatted(req.getCategoryId())
                ));

        if (!Objects.equals(req.getName(), unit.getName()) && salesUnitRepository.existsByName(req.getName())) {
            throw new UniqueValueExistsException("Единица продажи с таким именем уже существует");
        }

        unit.setName(req.getName());
        unit.setDescription(req.getDescription());
        unit.setCategory(category);
        unit.setCustomCommissionPercent(Double.valueOf(req.getCommissionPercent()));
        unit.setIsCustomCommission(req.getCommissionPercent() != null);

        return modelMapper.map(unit, SalesUnitResp.class);
    }

    @Transactional
    public SalesUnitResp resetToDefaultCommission(Long unitId, Long version) {

        SalesUnit unit = salesUnitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Единица продажи с id %d не была найдена".formatted(unitId)
                ));

        checkLock(version, unit.getVersion());

        unit.setCustomCommissionPercent(null);
        unit.setIsCustomCommission(false);

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

    @Transactional(readOnly = true)
    public Page<SalesUnitResp> getAllUnitsByCategory(SalesCategory category, Pageable pageable) {
        return salesUnitRepository.findAllByCategory(category, pageable).map(x -> modelMapper.map(x, SalesUnitResp.class));
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
