package com.Harshal.report_generator.exception;

import com.Harshal.report_generator.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ReportNotFoundException ex){
        return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponseDTO(404, ex.getMessage()));

    }

     @ExceptionHandler(ReportNotReadyException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotReady(ReportNotReadyException ex){
        return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(new ErrorResponseDTO(409, ex.getMessage()));

    }


     @ExceptionHandler(ReportExpiredException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ReportExpiredException ex){
        return ResponseEntity
        .status(HttpStatus.GONE)
        .body(new ErrorResponseDTO(410, ex.getMessage()));

    }



@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDTO(500, "An unexpected error occurred."));
}

    
}
