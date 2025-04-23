package ru.aviasales.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.aviasales.common.dao.entity.AdType;
import ru.aviasales.common.dao.entity.Advertisement;
import ru.aviasales.common.dao.entity.AdvertisementTaskStatus;
import ru.aviasales.common.dao.entity.UserSegment;
import ru.aviasales.common.domain.TaskStatus;
import ru.aviasales.processor.dao.repository.AdTypeRepository;
import ru.aviasales.processor.dao.repository.AdvertisementRepository;
import ru.aviasales.processor.dao.repository.AdvertisementTaskStatusRepository;
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
    private final AdvertisementTaskStatusRepository taskStatusRepository;

    public void processAdvertisementCreation(AdvertisementReq req) {
        if (req.getTaskId() == null) {
            log.error("Received AdvertisementReq without taskId: {}", req);
            throw new IllegalArgumentException("Task ID is missing");
        }
        String taskId = req.getTaskId();
        log.info("Processing advertisement creation request: {}", req);

        updateTaskStatus(taskId, TaskStatus.PROCESSING, null);

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
                    .payed(false)
                    .build();

            Advertisement savedAd = advertisementRepository.save(advertisement);
            log.info("Successfully created advertisement with ID: {} and Title: {}", savedAd.getId(), savedAd.getTitle());
            updateTaskStatus(taskId, TaskStatus.SUCCESS, null);
        } catch (EntityNotFoundException | IllegalOperationException e) {
            log.error("Validation failed during advertisement processing for request [Title: {}]: {}", req.getTitle(), e.getMessage());
            updateTaskStatus(taskId, TaskStatus.FAIL, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing advertisement creation request [Title: {}]: {}", req.getTitle(), e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAIL, e.getMessage());
            throw new RuntimeException("Failed to process advertisement creation: " + e.getMessage(), e); // Re-throw
        }
    }

    protected void updateTaskStatus(String taskId, TaskStatus status, String errorMessage) {
        try {
            AdvertisementTaskStatus taskStatus = taskStatusRepository.findById(taskId)
                    .orElse(null);

            if (taskStatus == null) {
                log.error("Cannot update status for Task ID {}: TaskStatus entity not found.", taskId);
                taskStatus = AdvertisementTaskStatus.builder().id(taskId).build();
            }

            taskStatus.setStatus(status);
            taskStatus.setErrorMessage(errorMessage);
            taskStatusRepository.save(taskStatus);
            log.info("Updated status for Task ID {} to {}", taskId, status);
        } catch (Exception ex) {
            log.error("Failed to update status for Task ID {} to {}: {}", taskId, status, ex.getMessage(), ex);
        }
    }
}