package com.Harshal.report_generator.config;

import com.Harshal.report_generator.service.ReportCleanupJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail reportCleanupJobDetail() {
        return JobBuilder.newJob(ReportCleanupJob.class)
                .withIdentity("reportCleanupJob")
                .withDescription("Finds and deletes expired report files")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger reportCleanupTrigger(JobDetail reportCleanupJobDetail) {
        return TriggerBuilder.newTrigger() // ← TriggerBuilder, NOT JobBuilder
                .forJob(reportCleanupJobDetail)
                .withIdentity("reportCleanupTrigger")
                .withDescription("Fires every hour to clean up expired reports")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 * * * * ?"))
                .build();
    }

}
