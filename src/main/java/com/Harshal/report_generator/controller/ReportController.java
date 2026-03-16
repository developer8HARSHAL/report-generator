package com.Harshal.report_generator.controller;

import com.Harshal.report_generator.dto.ReportRequestDTO;
import com.Harshal.report_generator.dto.ReportStatusDTO;
import com.Harshal.report_generator.model.ReportJob;
import com.Harshal.report_generator.model.ReportStatus;
import com.Harshal.report_generator.service.ReportService;


import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor

public class ReportController {
    
    private final ReportService reportService;



    @PostMapping("/request")
    public ResponseEntity<ReportStatusDTO>requestReport(
      @RequestBody ReportRequestDTO request) {

        ReportStatusDTO response = reportService.createReportJob(request);

        // ResponseEntity.accepted() = HTTP 202 Accepted
        return ResponseEntity.accepted().body(response);
    }


     @GetMapping("/{jobId}/status")
    public ResponseEntity<ReportStatusDTO> getStatus(
            @PathVariable String jobId) {

        ReportStatusDTO status = reportService.getJobStatus(jobId);

        // ResponseEntity.ok() = HTTP 200 OK
        return ResponseEntity.ok(status);
    }



@GetMapping("/{jobId}/download")
public ResponseEntity<?> downloadReport(@PathVariable String jobId) {

    // Fetch the full job entity from DB
    ReportJob job = reportService.getJobById(jobId);

    // Handle EXPIRED — file is gone
    if (job.getStatus() == ReportStatus.EXPIRED) {
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body("Report has expired. Please request a new one.");
    }

    // Handle NOT DONE — file not ready yet
    if (job.getStatus() != ReportStatus.DONE) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Report not ready yet. Current status: "
                        + job.getStatus().name());
    }

    // Job is DONE — stream the file back to client
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


      @GetMapping
    public ResponseEntity<List<ReportStatusDTO>> getMyReports(
            @RequestParam String requestedBy) {

        List<ReportStatusDTO> reports =
                reportService.getJobsByUser(requestedBy);

        return ResponseEntity.ok(reports);
    }

}
