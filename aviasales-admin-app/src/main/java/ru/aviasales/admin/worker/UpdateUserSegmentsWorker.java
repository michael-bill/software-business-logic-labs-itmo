package ru.aviasales.admin.worker;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aviasales.admin.dao.repository.UserSegmentRepository;
import ru.aviasales.common.dao.entity.UserSegment;

import java.util.List;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateUserSegmentsWorker {

    private final ExternalTaskClient client;
    private final UserSegmentRepository userSegmentRepository;
    private final Random random = new Random();

    @PostConstruct
    public void subscribe() {
        client.subscribe("update-user-segments")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    log.info("Worker 'update-user-segments': starting task processing");

                    try {
                        List<UserSegment> segments = userSegmentRepository.findAll();
                        if (segments.isEmpty()) {
                            log.info("Worker 'update-user-segments': no user segments found");
                            externalTaskService.complete(externalTask);
                            return;
                        }

                        int updatedCount = 0;
                        for (UserSegment segment : segments) {
                            int randomAmount = random.nextInt(99001) + 1000;
                            segment.setEstimatedAmount(randomAmount);
                            userSegmentRepository.save(segment);
                            updatedCount++;
                        }

                        log.info("Worker 'update-user-segments': updated {} segments", updatedCount);
                        externalTaskService.complete(externalTask);

                    } catch (Exception e) {
                        log.error("Worker 'update-user-segments': error updating segments", e);
                        externalTaskService.handleFailure(
                                externalTask,
                                "Ошибка обновления сегментов",
                                e.getMessage(),
                                0,
                                0
                        );
                    }
                })
                .open();
    }
}
