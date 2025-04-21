package ru.aviasales.admin.configuration;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import ru.aviasales.admin.job.UpdateUserSegmentsJob;

@Configuration
@Slf4j
public class QuartzConfig {

    @Value("${job.user-segment-update.interval-in-hours}")
    private int intervalInHours;

    private final ApplicationContext applicationContext;

    public QuartzConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean scheduler(Trigger trigger, JobDetail jobDetail) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setJobDetails(jobDetail);
        schedulerFactory.setTriggers(trigger);
        return schedulerFactory;
    }

    @Bean
    public JobDetail updateUserSegmentsJobDetail() {
        return JobBuilder.newJob(UpdateUserSegmentsJob.class)
                .withIdentity("updateUserSegmentsJob", "segment-jobs")
                .withDescription("Periodically updates the estimated amount for user segments.")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger updateUserSegmentsTrigger(JobDetail jobDetail) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(intervalInHours)
                .repeatForever();

        log.info("Configuring Quartz trigger for job '{}' with SimpleSchedule: interval 20 seconds, repeat forever.", jobDetail.getKey().getName());

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("updateUserSegmentsTrigger", "segment-triggers")
                .withDescription("Trigger for updating user segments.")
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();
    }

    private static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {
        private ApplicationContext ctx;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) {
            this.ctx = applicationContext;
        }

        @Override
        protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            ctx.getAutowireCapableBeanFactory().autowireBean(job);
            return job;
        }
    }
}
