package com.ktb.hackathon.entity;

import com.ktb.hackathon.entity.enums.RecordingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
	name = "recordings",
	uniqueConstraints = @UniqueConstraint(name = "uk_recording_session", columnNames = "counseling_session_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recording extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "counseling_session_id", nullable = false, unique = true)
	private CounselingSession counselingSession;

	@Column(name = "storage_key", length = 512)
	private String storageKey;

	@Column(name = "mime_type", length = 100)
	private String mimeType;

	@Column(name = "file_size_bytes")
	private Long fileSizeBytes;

	@Column(name = "duration_millis")
	private Long durationMillis;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RecordingStatus status;

	@Builder
	private Recording(
		CounselingSession counselingSession,
		String storageKey,
		String mimeType,
		Long fileSizeBytes,
		Long durationMillis
	) {
		this.counselingSession = counselingSession;
		this.storageKey = storageKey;
		this.mimeType = mimeType;
		this.fileSizeBytes = fileSizeBytes;
		this.durationMillis = durationMillis;
		this.status = RecordingStatus.PENDING;
	}
}
