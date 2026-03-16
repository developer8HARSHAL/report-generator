package com.Harshal.report_generator.exception;

public class ReportExpiredException extends RuntimeException {
     public ReportExpiredException(){
        super("Report has expired, please request a new one");
    }
    
}
