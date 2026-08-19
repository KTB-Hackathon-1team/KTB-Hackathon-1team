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

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "situation_text", nullable = false, columnDefinition = "LONGTEXT")
	private String content;

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
		String title,
		String content,
		LocalDateTime recordingConsentAt
	) {
		this.childProfile = childProfile;
		this.title = title;
		this.content = content;
		this.recordingConsentAt = recordingConsentAt;
		this.status = CounselingStatus.DRAFT;
	}

	public boolean canStartRecording() {
		return status == CounselingStatus.DRAFT || status == CounselingStatus.FAILED;
	}

	public void startRecording() {
		if (!canStartRecording()) {
			throw new IllegalStateException("현재 상담 상태에서는 녹음을 시작할 수 없습니다.");
		}

		this.status = CounselingStatus.RECORDING;
		this.startedAt = LocalDateTime.now();
		this.endedAt = null;
	}

	public void markTranscribing() {
		requireStatus(CounselingStatus.RECORDING);
		this.status = CounselingStatus.TRANSCRIBING;
		this.endedAt = LocalDateTime.now();
	}

	public void markAnalyzing() {
		requireStatus(CounselingStatus.TRANSCRIBING);
		this.status = CounselingStatus.ANALYZING;
	}

	public void complete() {
		requireStatus(CounselingStatus.ANALYZING);
		this.status = CounselingStatus.COMPLETED;
	}

	public void fail() {
		if (status == CounselingStatus.COMPLETED) {
			throw new IllegalStateException("완료된 상담은 실패 상태로 변경할 수 없습니다.");
		}

		this.status = CounselingStatus.FAILED;
		if (this.endedAt == null) {
			this.endedAt = LocalDateTime.now();
		}
	}

	private void requireStatus(CounselingStatus expectedStatus) {
		if (this.status != expectedStatus) {
			throw new IllegalStateException(
				"상담 상태가 " + expectedStatus + "일 때만 상태를 변경할 수 있습니다."
			);
		}
	}
}
