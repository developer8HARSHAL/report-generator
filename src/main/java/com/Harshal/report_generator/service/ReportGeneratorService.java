package com.Harshal.report_generator.service;

import com.Harshal.report_generator.model.ReportJob;
import com.Harshal.report_generator.model.ReportStatus;
import com.Harshal.report_generator.repository.ReportJobRepository;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Runs in a background thread (via @Async).
 * Responsible for generating the actual report file.
 */
@Service
public class ReportGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ReportGeneratorService.class);
    private static final String REPORTS_DIR = "reports";

    private final ReportJobRepository reportJobRepository;

    public ReportGeneratorService(ReportJobRepository reportJobRepository) {
        this.reportJobRepository = reportJobRepository;
    }

    @Async
    public void generateReport(ReportJob reportJob) {

        // ✅ Fetch a FRESH copy from DB — don't use the passed-in object
        // The passed-in object is "stale" because the transaction that
        // created it has already closed by the time this thread runs
        ReportJob job = reportJobRepository.findByJobId(reportJob.getJobId())
                .orElseThrow(() -> new RuntimeException(
                        "Job not found: " + reportJob.getJobId()));

        // Step 1: Mark as PROCESSING
        job.setStatus(ReportStatus.PROCESSING);
        reportJobRepository.save(job);
        log.info("Started processing job: {}", job.getJobId());

        try {
            // Step 2: Create folder for this job
            Path jobDir = Paths.get(REPORTS_DIR, job.getJobId());
            Files.createDirectories(jobDir);

            // Step 3: Build file name
            String fileName = job.getReportType().toLowerCase() + "_report.csv";
            Path filePath = jobDir.resolve(fileName);

            // Step 4: Generate the correct report
            switch (job.getReportType()) {
                case "SALES" -> generateSalesReport(filePath);
                case "USER_ACTIVITY" -> generateUserActivityReport(filePath);
                case "INVENTORY" -> generateInventoryReport(filePath);
                default -> throw new IllegalArgumentException(
                        "Unknown report type: " + job.getReportType());
            }

            // Step 5: Mark as DONE
            job.setStatus(ReportStatus.DONE);
            job.setFilePath(filePath.toString());
            job.setFileName(fileName);
            job.setCompletedAt(LocalDateTime.now());
            job.setExpiresAt(LocalDateTime.now().plusHours(24));
            reportJobRepository.save(job);

            log.info("Job completed: {} | File: {}", job.getJobId(), filePath);

        } catch (Exception e) {
            log.error("Job failed: {} | Reason: {}", job.getJobId(), e.getMessage());
            job.setStatus(ReportStatus.FAILED);
            job.setErrorMsg(e.getMessage());
            reportJobRepository.save(job);
        }
    }

    private void generateSalesReport(Path filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
            writer.writeNext(new String[] { "Product", "Region", "Amount", "Sold At" });
            writer.writeNext(new String[] { "Laptop", "North", "75000", "2024-01-15" });
            writer.writeNext(new String[] { "Phone", "South", "45000", "2024-02-20" });
            writer.writeNext(new String[] { "Tablet", "East", "32000", "2024-03-10" });
            writer.writeNext(new String[] { "Monitor", "West", "18000", "2024-04-05" });
            writer.writeNext(new String[] { "Keyboard", "North", "3500", "2024-05-01" });
            Thread.sleep(3000); // simulates heavy report generation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Report generation interrupted", e);
        }
    }

    private void generateUserActivityReport(Path filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
            writer.writeNext(new String[] { "User", "Action", "Timestamp" });
            writer.writeNext(new String[] { "alice@example.com", "LOGIN", "2024-01-10 09:00" });
            writer.writeNext(new String[] { "bob@example.com", "PURCHASE", "2024-01-10 10:30" });
            writer.writeNext(new String[] { "alice@example.com", "LOGOUT", "2024-01-10 11:00" });
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Report generation interrupted", e);
        }
    }

    private void generateInventoryReport(Path filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
            writer.writeNext(new String[] { "Item", "Quantity", "Warehouse" });
            writer.writeNext(new String[] { "Laptop", "120", "Mumbai" });
            writer.writeNext(new String[] { "Phone", "340", "Delhi" });
            writer.writeNext(new String[] { "Tablet", "85", "Pune" });
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Report generation interrupted", e);
        }
    }
}