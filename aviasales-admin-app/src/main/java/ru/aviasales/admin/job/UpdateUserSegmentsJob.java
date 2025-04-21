package ru.aviasales.admin.job;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.repository.UserSegmentRepository;
import ru.aviasales.common.dao.entity.UserSegment;

import java.util.List;
import java.util.Random;

@Component
@NoArgsConstructor(force = true)
@Slf4j
public class UpdateUserSegmentsJob extends QuartzJobBean {

    @Autowired
    private final UserSegmentRepository userSegmentRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (userSegmentRepository == null) {
            log.error("UserSegmentRepository is null. Autowiring failed?");
            throw new JobExecutionException("UserSegmentRepository not injected.", false);
        }

        log.info("Starting scheduled job: UpdateUserSegmentsJob");

        try {
            List<UserSegment> segments = userSegmentRepository.findAll();
            if (segments.isEmpty()) {
                log.info("No user segments found to update.");
                return;
            }

            int updatedCount = 0;
            for (UserSegment segment : segments) {
                int randomAmount = random.nextInt(99001) + 1000;
                segment.setEstimatedAmount(randomAmount);
                userSegmentRepository.save(segment);
                updatedCount++;
            }

            log.info("Finished scheduled job: UpdateUserSegmentsJob. Updated {} segments.", updatedCount);

        } catch (Exception e) {
            log.error("Error executing UpdateUserSegmentsJob", e);
        }
    }
}
