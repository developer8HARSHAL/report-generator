package com.Harshal.report_generator;

import com.Harshal.report_generator.dto.ReportRequestDTO;
import com.Harshal.report_generator.dto.ReportStatusDTO;
import com.Harshal.report_generator.exception.ReportNotFoundException;
import com.Harshal.report_generator.model.ReportJob;
import com.Harshal.report_generator.model.ReportStatus;
import com.Harshal.report_generator.repository.ReportJobRepository;
import com.Harshal.report_generator.service.ReportGeneratorService;
import com.Harshal.report_generator.service.ReportService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportJobRepository reportJobRepository;

    @Mock
    private ReportGeneratorService reportGeneratorService;

    @InjectMocks
    private ReportService reportService;



    @Test
    void createReportJob_shouldSaveJobWithStatusQueued() {

        // ARRANGE — build the incoming request
        ReportRequestDTO request = new ReportRequestDTO();
        request.setReportType("SALES");
        request.setRequestedBy("harshal@example.com");

        when(reportJobRepository.save(any(ReportJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReportStatusDTO result = reportService.createReportJob(request);

        assertNotNull(result.getJobId());                      
        assertEquals(ReportStatus.QUEUED, result.getStatus());    
        assertEquals("harshal@example.com", result.getRequestedBy());

        // Verify save() was called exactly once
        verify(reportJobRepository, times(1)).save(any(ReportJob.class));
    }

    @Test
    void getJobStatus_shouldThrowException_whenJobNotFound() {

        when(reportJobRepository.findByJobId("non-existent-id"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT — verify that the right exception is thrown
        assertThrows(
            ReportNotFoundException.class,
            () -> reportService.getJobStatus("non-existent-id")
        );
    }


    @Test
    void getJobStatus_shouldReturnStatus_whenJobExists() {

        // ARRANGE — create a fake job that the mock will return
        ReportJob fakeJob = new ReportJob();
        fakeJob.setJobId("abc-123");
        fakeJob.setStatus(ReportStatus.PROCESSING);
        fakeJob.setRequestedBy("harshal@example.com");

        when(reportJobRepository.findByJobId("abc-123"))
                .thenReturn(Optional.of(fakeJob));

        ReportStatusDTO result = reportService.getJobStatus("abc-123");

        // ASSERT
        assertEquals("abc-123", result.getJobId());
        assertEquals(ReportStatus.PROCESSING, result.getStatus());
    }
}