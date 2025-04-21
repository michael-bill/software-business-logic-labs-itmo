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
    private final AdTypeRepository adTypeRepository;
    private final UserSegmentRepository userSegmentRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public AdvertisementResp createAdvertisement(AdvertisementReq req) {
        AdType adType = adTypeRepository.findById(req.getAdTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Тип рекламы не найден"));
        if(!adType.getActive()) {
            throw new IllegalOperationException("Тип рекламы на данный момент отключен");
        }

        Set<UserSegment> targetSegments = new HashSet<>();
        if(adType.getSupportsSegmentation()) {
            if (req.getTargetSegmentIds() != null) {
                for (Long segmentId : req.getTargetSegmentIds()) {
                    UserSegment segment = userSegmentRepository.findById(segmentId)
                            .orElseThrow(() -> new EntityNotFoundException("Сегмент пользователей не найден"));
                    targetSegments.add(segment);
                }
            }
        } else if(req.getTargetSegmentIds() != null) {
            throw new IllegalOperationException("Тип рекламы не поддерживает сегментацию");
        }

        if (req.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalOperationException("Дата окончания рекламы не может быть раньше текущего времени");
        }

        Advertisement advertisement = Advertisement.builder()
                .title(req.getTitle())
                .companyName(req.getCompanyName())
                .description(req.getDescription())
                .adType(adType)
                .targetSegments(targetSegments)
                .deadline(req.getDeadline())
                .build();

        return modelMapper.map(advertisementRepository.save(advertisement), AdvertisementResp.class);
    }


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