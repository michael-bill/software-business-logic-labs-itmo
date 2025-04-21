package ru.aviasales.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.common.dao.entity.AdType;
import ru.aviasales.common.dao.entity.Advertisement;
import ru.aviasales.common.dao.entity.UserSegment;
import ru.aviasales.processor.dao.repository.AdTypeRepository;
import ru.aviasales.processor.dao.repository.AdvertisementRepository;
import ru.aviasales.processor.dao.repository.UserSegmentRepository;
import ru.aviasales.common.dto.request.AdvertisementReq;
import ru.aviasales.processor.exception.EntityNotFoundException;
import ru.aviasales.processor.exception.IllegalOperationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementProcessingService {

    private final AdvertisementRepository advertisementRepository;
    private final AdTypeRepository adTypeRepository;
    private final UserSegmentRepository userSegmentRepository;

    @Transactional
    public void processAdvertisementCreation(AdvertisementReq req) {
        log.info("Processing advertisement creation request: {}", req);

        try {
            AdType adType = adTypeRepository.findById(req.getAdTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Тип рекламы не найден: ID " + req.getAdTypeId()));
            if (!adType.getActive()) {
                throw new IllegalOperationException("Тип рекламы на данный момент отключен: ID " + req.getAdTypeId());
            }

            Set<UserSegment> targetSegments = new HashSet<>();
            boolean requiresSegmentation = adType.getSupportsSegmentation() != null && adType.getSupportsSegmentation();

            if (requiresSegmentation) {
                if (req.getTargetSegmentIds() == null || req.getTargetSegmentIds().isEmpty()) {
                    log.warn("Ad type {} supports segmentation, but no target segments provided for request: {}", adType.getId(), req.getTitle());
                } else {
                    for (Long segmentId : req.getTargetSegmentIds()) {
                        UserSegment segment = userSegmentRepository.findById(segmentId)
                                .orElseThrow(() -> new EntityNotFoundException("Сегмент пользователей не найден: ID " + segmentId));
                        targetSegments.add(segment);
                    }
                    log.debug("Found {} target segments for advertisement '{}'", targetSegments.size(), req.getTitle());
                }
            } else if (req.getTargetSegmentIds() != null && !req.getTargetSegmentIds().isEmpty()) {
                log.warn("Ad type {} does not support segmentation, but target segments were provided for request: {}. Ignoring segments.", adType.getId(), req.getTitle());
            }


            if (req.getDeadline() != null && req.getDeadline().isBefore(LocalDateTime.now())) {
                throw new IllegalOperationException("Дата окончания рекламы не может быть раньше текущего времени: " + req.getDeadline());
            }

            Advertisement advertisement = Advertisement.builder()
                    .title(req.getTitle())
                    .companyName(req.getCompanyName())
                    .description(req.getDescription())
                    .adType(adType)
                    .targetSegments(requiresSegmentation ? targetSegments : null)
                    .deadline(req.getDeadline())
                    .build();

            Advertisement savedAd = advertisementRepository.save(advertisement);
            log.info("Successfully created advertisement with ID: {} and Title: {}", savedAd.getId(), savedAd.getTitle());

        } catch (EntityNotFoundException | IllegalOperationException e) {
            log.error("Validation failed during advertisement processing for request [Title: {}]: {}", req.getTitle(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing advertisement creation request [Title: {}]: {}", req.getTitle(), e.getMessage(), e);
            throw new RuntimeException("Failed to process advertisement creation: " + e.getMessage(), e); // Re-throw
        }
    }
}