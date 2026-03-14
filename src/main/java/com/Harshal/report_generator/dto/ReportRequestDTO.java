package com.Harshal.report_generator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ReportRequestDTO {
    private String reportType;
    private String fromDate;
    private String toDate;
    private String requestedBy;
    
}
