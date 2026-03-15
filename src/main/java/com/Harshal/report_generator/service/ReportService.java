package com.Harshal.report_generator.service;

import com.Harshal.report_generator.dto.ReportRequestDTO;
import com.Harshal.report_generator.dto.ReportStatusDTO;
import com.Harshal.report_generator.model.ReportJob;
import com.Harshal.report_generator.model.ReportStatus;
import com.Harshal.report_generator.repository.ReportJobRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportJobRepository reportJobRepository;
    private final ReportGeneratorService reportGeneratorService; // ← CHANGE 1

    @Transactional
    public ReportStatusDTO createReportJob(ReportRequestDTO request) {
        String jobId = UUID.randomUUID().toString();

        ReportJob job = ReportJob.builder()
                .jobId(jobId)
                .reportType(request.getReportType())
                .requestedBy(request.getRequestedBy())
                .parameters(buildParameters(request))
                .build();

        reportJobRepository.save(job);
        reportGeneratorService.generateReport(job); // ← CHANGE 2

        return ReportStatusDTO.builder()
                .jobId(jobId)
                .status(ReportStatus.QUEUED)
                .reportType(request.getReportType())
                .requestedBy(request.getRequestedBy())
                .createdAt(job.getCreatedAt())
                .message("Report queued successfully. " +
                         "Poll /api/reports/" + jobId + "/status for updates.")
                .build();
    }

    public ReportStatusDTO getJobStatus(String jobId) {
        ReportJob job = reportJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException(
                        "Job not found with id: " + jobId));
        return mapToStatusDTO(job);
    }

    public List<ReportStatusDTO> getJobsByUser(String requestedBy) {
        List<ReportJob> jobs = reportJobRepository.findByRequestedBy(requestedBy);
        return jobs.stream()
                .map(this::mapToStatusDTO)
                .collect(Collectors.toList());
    }

    // ← CHANGE 3: needed by the download endpoint
    public ReportJob getJobById(String jobId) {
        return reportJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
    }

    private ReportStatusDTO mapToStatusDTO(ReportJob job) {
        return ReportStatusDTO.builder()
                .jobId(job.getJobId())
                .status(job.getStatus())
                .reportType(job.getReportType())
                .requestedBy(job.getRequestedBy())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .message(buildMessage(job.getStatus()))
                .build();
    }

    private String buildParameters(ReportRequestDTO request) {
        return String.format(
                "{\"fromDate\":\"%s\",\"toDate\":\"%s\"}",
                request.getFromDate(),
                request.getToDate()
        );
    }

    private String buildMessage(ReportStatus status) {
        return switch (status) {
            case QUEUED     -> "Report is queued for processing.";
            case PROCESSING -> "Report is currently being generated.";
            case DONE       -> "Report is ready for download.";
            case FAILED     -> "Report generation failed.";
            case EXPIRED    -> "Report has expired. Please request again.";
            default         -> "Unknown status.";  

        };
    }
}


