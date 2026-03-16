package com.Harshal.report_generator.dto;

import java.time.LocalDateTime;

public class ErrorResponseDTO {

    private int status;
    private String error;
    private LocalDateTime timestamp;

    public ErrorResponseDTO(int status, String error){
        this.status=status;
        this.error=error;
        this.timestamp=LocalDateTime.now();
    }


    public int getStatus(){ return status;}
    public String getError(){return error;}
    public LocalDateTime getTimestamp(){return timestamp;}

    
    
}
