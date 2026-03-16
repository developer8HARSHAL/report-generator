package com.Harshal.report_generator.exception;

public class ReportNotReadyException extends RuntimeException{

    public ReportNotReadyException(String status){
        super("Report not ready yet :" + status);
    }
    
}
