// src/main/java/com/harshal/reportgenerator/service/ReportCleanupJob.java

package com.Harshal.report_generator.service;

import com.Harshal.report_generator.model.ReportJob;
import com.Harshal.report_generator.model.ReportStatus;
import com.Harshal.report_generator.repository.ReportJobRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;


@Component
public class ReportCleanupJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ReportCleanupJob.class);

    @Autowired
    private ReportJobRepository reportJobRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Report cleanup job started at {}", LocalDateTime.now());

  
        List<ReportJob> expiredJobs = reportJobRepository
                .findByStatusAndExpiresAtBefore(ReportStatus.DONE, LocalDateTime.now());

        logger.info("Found {} expired report(s) to clean up", expiredJobs.size());

        for (ReportJob job : expiredJobs) {
            try {
                deleteFileIfExists(job);

                job.setStatus(ReportStatus.EXPIRED);
                reportJobRepository.save(job);

                logger.info("Marked job {} as EXPIRED", job.getJobId());

            } catch (Exception e) {

                logger.error("Failed to clean up job {}: {}", job.getJobId(), e.getMessage());
            }
        }

        logger.info("Report cleanup job finished. Processed {} job(s)", expiredJobs.size());
    }


    private void deleteFileIfExists(ReportJob job) throws IOException {
        String filePath = job.getFilePath();

        if (filePath == null || filePath.isBlank()) {
            return;
        }

        Path path = Paths.get(filePath);

        if (Files.exists(path)) {
            Files.delete(path);
            logger.info("Deleted file: {}", filePath);
        } else {
            logger.warn("File not found on disk (already deleted?): {}", filePath);
        }
    }
}