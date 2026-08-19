package com.ktb.hackathon.entity;

import com.ktb.hackathon.entity.enums.CounselingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "counseling_sessions",
	indexes = {
		@Index(name = "idx_counseling_session_child_created", columnList = "child_profile_id, created_at"),
		@Index(name = "idx_counseling_session_status", columnList = "status")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselingSession extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "child_profile_id", nullable = false)
	private ChildProfile childProfile;

	@Column(name = "situation_text", nullable = false, columnDefinition = "LONGTEXT")
	private String situationText;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private CounselingStatus status;

	@Column(name = "recording_consent_at")
	private LocalDateTime recordingConsentAt;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "ended_at")
	private LocalDateTime endedAt;

	@Builder
	private CounselingSession(
		ChildProfile childProfile,
		String situationText,
		LocalDateTime recordingConsentAt
	) {
		this.childProfile = childProfile;
		this.situationText = situationText;
		this.recordingConsentAt = recordingConsentAt;
		this.status = CounselingStatus.DRAFT;
	}
}
