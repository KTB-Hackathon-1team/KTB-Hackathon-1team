package com.ktb.hackathon.entity;

import com.ktb.hackathon.entity.enums.MessageSpeaker;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "conversation_messages",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_conversation_message_sequence",
		columnNames = {"counseling_session_id", "sequence_no"}
	),
	indexes = @Index(
		name = "idx_conversation_message_session_sequence",
		columnList = "counseling_session_id, sequence_no"
	)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationMessage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "counseling_session_id", nullable = false)
	private CounselingSession counselingSession;

	@Column(name = "sequence_no", nullable = false)
	private Integer sequenceNo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MessageSpeaker speaker;

	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String content;

	@Column(name = "start_offset_millis")
	private Long startOffsetMillis;

	@Column(name = "end_offset_millis")
	private Long endOffsetMillis;

	@Builder
	private ConversationMessage(
		CounselingSession counselingSession,
		Integer sequenceNo,
		MessageSpeaker speaker,
		String content,
		Long startOffsetMillis,
		Long endOffsetMillis
	) {
		this.counselingSession = counselingSession;
		this.sequenceNo = sequenceNo;
		this.speaker = speaker;
		this.content = content;
		this.startOffsetMillis = startOffsetMillis;
		this.endOffsetMillis = endOffsetMillis;
	}
}
