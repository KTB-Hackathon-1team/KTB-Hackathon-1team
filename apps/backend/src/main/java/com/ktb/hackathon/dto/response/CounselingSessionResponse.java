package com.ktb.hackathon.dto.response;

import com.ktb.hackathon.entity.CounselingSession;
import java.time.LocalDate;

public record CounselingSessionResponse(
	Long id,
	LocalDate date,
	String title,
	String content
) {

	public static CounselingSessionResponse from(CounselingSession counselingSession) {
		return new CounselingSessionResponse(
			counselingSession.getId(),
			counselingSession.getCreatedAt().toLocalDate(),
			counselingSession.getTitle(),
			counselingSession.getContent()
		);
	}
}
