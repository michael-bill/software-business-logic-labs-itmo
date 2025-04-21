package ru.aviasales.admin.service.core.ad;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.aviasales.common.dao.entity.AdType;
import ru.aviasales.common.dao.entity.Advertisement;
import ru.aviasales.common.dao.entity.UserSegment;
import ru.aviasales.admin.dao.repository.AdTypeRepository;
import ru.aviasales.admin.dao.repository.AdvertisementRepository;
import ru.aviasales.admin.dao.repository.UserSegmentRepository;
import ru.aviasales.common.dto.request.AdvertisementReq;
import ru.aviasales.common.dto.response.AdvertisementResp;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.exception.EntityNotFoundException;
import ru.aviasales.admin.exception.IllegalOperationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public AdvertisementResp getAdvertisementById(Long advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .map(ad -> modelMapper.map(ad, AdvertisementResp.class))
                .orElseThrow(() -> new EntityNotFoundException("Рекламное объявление не найдено"));
    }

    @Transactional(readOnly = true)
    public Page<AdvertisementResp> getAllAdvertisements(Pageable pageable) {
        return advertisementRepository.findAll(pageable)
                .map(ad -> modelMapper.map(ad, AdvertisementResp.class));
    }

}