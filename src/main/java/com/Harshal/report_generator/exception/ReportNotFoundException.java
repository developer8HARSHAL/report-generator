package com.Harshal.report_generator.exception;

public class ReportNotFoundException extends RuntimeException{

    public ReportNotFoundException(String jobId){
        super("Report job not found: "+ jobId);
    }
    
}
