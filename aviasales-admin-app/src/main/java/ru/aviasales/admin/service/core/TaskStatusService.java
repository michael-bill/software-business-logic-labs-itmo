package ru.aviasales.admin.service.core;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.repository.AdvertisementTaskStatusRepository;
import ru.aviasales.admin.exception.EntityNotFoundException;
import ru.aviasales.admin.service.messaging.KafkaProducerService;
import ru.aviasales.common.dao.entity.AdvertisementTaskStatus;
import ru.aviasales.common.domain.TaskStatus;
import ru.aviasales.common.dto.request.AdvertisementReq;
import ru.aviasales.common.dto.response.CreateTaskResp;
import ru.aviasales.common.dto.response.TaskStatusResp;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskStatusService {

    private final AdvertisementTaskStatusRepository taskStatusRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ModelMapper modelMapper;

    @Transactional
    public CreateTaskResp initiateAdvertisementCreation(AdvertisementReq req) {
        String taskId = UUID.randomUUID().toString();
        req.setTaskId(taskId);

        AdvertisementTaskStatus taskStatus = AdvertisementTaskStatus.builder()
                .id(taskId)
                .status(TaskStatus.PENDING)
                .build();
        taskStatusRepository.save(taskStatus);

        kafkaProducerService.sendAdvertisementRequest(req);

        return CreateTaskResp.builder()
                .message("Запрос на создание рекламы принят и взят в обработку.")
                .taskId(taskId)
                .build();
    }

    @Transactional(readOnly = true)
    public TaskStatusResp getTaskStatus(String taskId) {
        AdvertisementTaskStatus taskStatus = taskStatusRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача с ID " + taskId + " не найдена."));
        return modelMapper.map(taskStatus, TaskStatusResp.class);
    }
}
