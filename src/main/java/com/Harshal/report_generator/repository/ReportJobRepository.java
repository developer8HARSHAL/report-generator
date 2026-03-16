package com.Harshal.report_generator.repository;
import com.Harshal.report_generator.model.ReportJob;
import com.Harshal.report_generator.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository

public interface ReportJobRepository extends JpaRepository <ReportJob, Long>{

    Optional<ReportJob>findByJobId ( String jobId);

    List<ReportJob>findByRequestedBy (String requestedBy);
List<ReportJob> findByStatusAndExpiresAtBefore(ReportStatus status, LocalDateTime dateTime);


    
}
