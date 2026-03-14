package com.Harshal.report_generator.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


    @Entity
    @Table(name="report_jobs")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder

public class ReportJob {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

     @Column(name = "job_id", nullable = false, unique = true, length = 36)
    private String jobId;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;


    @Column(name = "requested_by", length = 100)
    private String requestedBy;


    @Column(columnDefinition = "JSON")
    private String parameters;


    @Column(name = "file_path", length = 255)
    private String filePath;

   
    @Column(name = "file_name", length = 100)
    private String fileName;

   
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

   
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

  
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(24);
        this.status = ReportStatus.QUEUED;
    }
}