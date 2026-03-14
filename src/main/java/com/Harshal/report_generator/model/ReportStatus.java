package com.Harshal.report_generator.model;

 /* State flow:
 * QUEUED → PROCESSING → DONE → EXPIRED
 *                    ↘ FAILED
 */

public enum ReportStatus {
    QUEUED,
    PROCESSING,
    REJECTED,
    DONE,
    FAILED,
    EXPIRED    
}
