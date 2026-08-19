package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.AnalysisReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

	Optional<AnalysisReport> findByCounselingSessionId(Long counselingSessionId);
}
