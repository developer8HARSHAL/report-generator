package com.Harshal.report_generator.controller;

import com.Harshal.report_generator.dto.ReportRequestDTO;
import com.Harshal.report_generator.dto.ReportStatusDTO;
import com.Harshal.report_generator.service.ReportService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> downloadReport(
            @PathVariable String jobId) {

        // Placeholder — full file streaming logic comes in Day 3
        return ResponseEntity.ok("Download endpoint - coming in Day 3");
    }


      @GetMapping
    public ResponseEntity<List<ReportStatusDTO>> getMyReports(
            @RequestParam String requestedBy) {

        List<ReportStatusDTO> reports =
                reportService.getJobsByUser(requestedBy);

        return ResponseEntity.ok(reports);
    }

}
