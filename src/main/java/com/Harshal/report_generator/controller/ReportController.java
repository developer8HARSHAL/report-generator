package com.Harshal.report_generator.controller;

import com.Harshal.report_generator.dto.ReportRequestDTO;
import com.Harshal.report_generator.dto.ReportStatusDTO;
import com.Harshal.report_generator.model.ReportJob;
import com.Harshal.report_generator.model.ReportStatus;
import com.Harshal.report_generator.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report API", description = "Endpoints for requesting, tracking, and downloading reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
        summary = "Request a new report",
        description = "Submits a report generation job. Returns 202 Accepted with a jobId immediately. Report is generated asynchronously in the background."
    )
    @PostMapping("/request")
    public ResponseEntity<ReportStatusDTO> requestReport(
            @RequestBody ReportRequestDTO request) {

        ReportStatusDTO response = reportService.createReportJob(request);
        return ResponseEntity.accepted().body(response);
    }

    @Operation(
        summary = "Poll job status",
        description = "Returns the current status of a report job: QUEUED, PROCESSING, DONE, FAILED, or EXPIRED."
    )
    @GetMapping("/{jobId}/status")
    public ResponseEntity<ReportStatusDTO> getStatus(
            @PathVariable String jobId) {

        ReportStatusDTO status = reportService.getJobStatus(jobId);
        return ResponseEntity.ok(status);
    }

    @Operation(
        summary = "Download report file",
        description = "Streams the generated CSV file to the client. Returns 409 if report is not ready yet, 410 if the report has expired."
    )
    @GetMapping("/{jobId}/download")
    public ResponseEntity<?> downloadReport(@PathVariable String jobId) {

        ReportJob job = reportService.getJobById(jobId);

        if (job.getStatus() == ReportStatus.EXPIRED) {
            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body("Report has expired. Please request a new one.");
        }

        if (job.getStatus() != ReportStatus.DONE) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Report not ready yet. Current status: "
                            + job.getStatus().name());
        }

        try {
            Path filePath = Paths.get(job.getFilePath());
            FileInputStream fileStream = new FileInputStream(filePath.toFile());
            InputStreamResource resource = new InputStreamResource(fileStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + job.getFileName() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File not found on server. Please request a new report.");
        }
    }

    @Operation(
        summary = "List reports by user",
        description = "Returns all report jobs submitted by a specific user, identified by email or userId."
    )
    @GetMapping
    public ResponseEntity<List<ReportStatusDTO>> getMyReports(
            @RequestParam String requestedBy) {

        List<ReportStatusDTO> reports = reportService.getJobsByUser(requestedBy);
        return ResponseEntity.ok(reports);
    }
}