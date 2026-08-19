package com.ktb.hackathon.dto.response;

import com.ktb.hackathon.entity.AnalysisReport;
import com.ktb.hackathon.entity.CounselingSession;
import com.ktb.hackathon.entity.enums.CounselingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CounselingSessionDetailResponse(
	Long id,
	LocalDate date,
	String title,
	String content,
	CounselingStatus status,
	LocalDateTime startedAt,
	LocalDateTime endedAt,
	AnalysisReportResponse analysisReport
) {

	public static CounselingSessionDetailResponse from(
		CounselingSession counselingSession,
		AnalysisReport analysisReport
	) {
		return new CounselingSessionDetailResponse(
			counselingSession.getId(),
			counselingSession.getCreatedAt().toLocalDate(),
			counselingSession.getTitle(),
			counselingSession.getContent(),
			counselingSession.getStatus(),
			counselingSession.getStartedAt(),
			counselingSession.getEndedAt(),
			AnalysisReportResponse.from(analysisReport)
		);
	}
}
