package com.Harshal.report_generator.dto;

import java.time.LocalDateTime;

import com.Harshal.report_generator.model.ReportStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ReportStatusDTO {
    private String jobId;
    private ReportStatus status;
    private String reportType;
    private String requestedBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String message;

}
