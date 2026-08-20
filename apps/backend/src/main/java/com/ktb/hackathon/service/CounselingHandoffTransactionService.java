package com.ktb.hackathon.service;

import com.ktb.hackathon.auth.AuthenticatedUser;
import com.ktb.hackathon.dto.request.CounselingHandoffRequest;
import com.ktb.hackathon.dto.request.CounselingTurnRequest;
import com.ktb.hackathon.entity.ConversationMessage;
import com.ktb.hackathon.entity.CounselingSession;
import com.ktb.hackathon.entity.enums.CounselingStatus;
import com.ktb.hackathon.entity.enums.MessageSpeaker;
import com.ktb.hackathon.exception.AuthException;
import com.ktb.hackathon.exception.SummarizationException;
import com.ktb.hackathon.repository.AnalysisReportRepository;
import com.ktb.hackathon.repository.ConversationMessageRepository;
import com.ktb.hackathon.repository.CounselingSessionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounselingHandoffTransactionService {

	private final CounselingSessionRepository counselingSessionRepository;
	private final ConversationMessageRepository conversationMessageRepository;
	private final AnalysisReportRepository analysisReportRepository;

	public CounselingHandoffTransactionService(
		CounselingSessionRepository counselingSessionRepository,
		ConversationMessageRepository conversationMessageRepository,
		AnalysisReportRepository analysisReportRepository
	) {
		this.counselingSessionRepository = counselingSessionRepository;
		this.conversationMessageRepository = conversationMessageRepository;
		this.analysisReportRepository = analysisReportRepository;
	}

	@Transactional
	public void saveConversationAndStartAnalysis(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		Long sessionId,
		CounselingHandoffRequest request
	) {
		CounselingSession counselingSession = findOwnedSession(
			authenticatedUser,
			childProfileId,
			sessionId
		);

		if (counselingSession.getStatus() != CounselingStatus.RECORDING) {
			throw new AuthException(
				HttpStatus.CONFLICT,
				"COUNSELING_HANDOFF_NOT_ALLOWED",
				"RECORDING 상태의 상담 세션만 대화를 저장할 수 있습니다."
			);
		}

		List<ConversationMessage> messages = new ArrayList<>();
		int sequenceNo = 1;
		for (CounselingTurnRequest turn : request.turns()) {
			messages.add(ConversationMessage.builder()
				.counselingSession(counselingSession)
				.sequenceNo(sequenceNo++)
				.speaker(toSpeaker(turn.role()))
				.content(turn.text())
				.build());
		}

		conversationMessageRepository.saveAll(messages);
		counselingSession.markTranscribing();
		counselingSession.markAnalyzing();
		conversationMessageRepository.flush();
	}

	@Transactional
	public void completeAnalysis(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		Long sessionId
	) {
		CounselingSession counselingSession = findOwnedSession(
			authenticatedUser,
			childProfileId,
			sessionId
		);

		if (counselingSession.getStatus() != CounselingStatus.ANALYZING) {
			throw new SummarizationException(
				HttpStatus.BAD_GATEWAY,
				"상담 요약을 완료할 수 없는 상태입니다."
			);
		}

		if (analysisReportRepository.findByCounselingSessionId(sessionId).isEmpty()) {
			throw new SummarizationException(
				HttpStatus.BAD_GATEWAY,
				"상담 분석 결과를 찾을 수 없습니다."
			);
		}

		counselingSession.complete();
	}

	@Transactional
	public void markFailed(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		Long sessionId
	) {
		CounselingSession counselingSession = findOwnedSession(
			authenticatedUser,
			childProfileId,
			sessionId
		);

		if (counselingSession.getStatus() != CounselingStatus.COMPLETED) {
			counselingSession.fail();
		}
	}

	private MessageSpeaker toSpeaker(String role) {
		return switch (role) {
			case "user" -> MessageSpeaker.CHILD;
			case "assistant" -> MessageSpeaker.AI;
			default -> throw new AuthException(
				HttpStatus.BAD_REQUEST,
				"INVALID_CONVERSATION_ROLE",
				"role은 user 또는 assistant만 사용할 수 있습니다."
			);
		};
	}

	private CounselingSession findOwnedSession(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		Long sessionId
	) {
		return counselingSessionRepository.findByIdAndChildProfileIdAndChildProfileParentAccountId(
			sessionId,
			childProfileId,
			authenticatedUser.parentAccountId()
		).orElseThrow(() -> new AuthException(
			HttpStatus.NOT_FOUND,
			"COUNSELING_SESSION_NOT_FOUND",
			"상담 세션을 찾을 수 없습니다."
		));
	}
}
