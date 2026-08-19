package com.ktb.hackathon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "analysis_reports",
	uniqueConstraints = @UniqueConstraint(name = "uk_analysis_report_session", columnNames = "counseling_session_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReport extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "counseling_session_id", nullable = false, unique = true)
	private CounselingSession counselingSession;

	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String summary;

	@Column(name = "emotion_summary", nullable = false, columnDefinition = "LONGTEXT")
	private String emotionSummary;

	@Column(name = "parenting_guidance", nullable = false, columnDefinition = "LONGTEXT")
	private String parentingGuidance;

	@Lob
	@Column(name = "result_payload", nullable = false)
	private String resultPayload;

	@Column(name = "model_name", nullable = false, length = 100)
	private String modelName;

	@Column(name = "prompt_version", nullable = false, length = 50)
	private String promptVersion;

	@Builder
	private AnalysisReport(
		CounselingSession counselingSession,
		String summary,
		String emotionSummary,
		String parentingGuidance,
		String resultPayload,
		String modelName,
		String promptVersion
	) {
		this.counselingSession = counselingSession;
		this.summary = summary;
		this.emotionSummary = emotionSummary;
		this.parentingGuidance = parentingGuidance;
		this.resultPayload = resultPayload;
		this.modelName = modelName;
		this.promptVersion = promptVersion;
	}
}
