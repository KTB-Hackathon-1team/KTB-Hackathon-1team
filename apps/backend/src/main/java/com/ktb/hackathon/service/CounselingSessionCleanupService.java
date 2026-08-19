package com.ktb.hackathon.service;

import com.ktb.hackathon.entity.CounselingSession;
import com.ktb.hackathon.repository.AnalysisReportRepository;
import com.ktb.hackathon.repository.ConversationMessageRepository;
import com.ktb.hackathon.repository.CounselingSessionRepository;
import com.ktb.hackathon.repository.RecordingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounselingSessionCleanupService {

	private final ConversationMessageRepository conversationMessageRepository;
	private final AnalysisReportRepository analysisReportRepository;
	private final RecordingRepository recordingRepository;
	private final CounselingSessionRepository counselingSessionRepository;

	public CounselingSessionCleanupService(
		ConversationMessageRepository conversationMessageRepository,
		AnalysisReportRepository analysisReportRepository,
		RecordingRepository recordingRepository,
		CounselingSessionRepository counselingSessionRepository
	) {
		this.conversationMessageRepository = conversationMessageRepository;
		this.analysisReportRepository = analysisReportRepository;
		this.recordingRepository = recordingRepository;
		this.counselingSessionRepository = counselingSessionRepository;
	}

	@Transactional
	public String delete(CounselingSession counselingSession) {
		Long sessionId = counselingSession.getId();
		conversationMessageRepository.deleteAllByCounselingSessionId(sessionId);
		analysisReportRepository.findByCounselingSessionId(sessionId)
			.ifPresent(analysisReportRepository::delete);

		String recordingKey = recordingRepository.findByCounselingSessionId(sessionId)
			.map(recording -> {
				String storageKey = recording.getStorageKey();
				recordingRepository.delete(recording);
				return storageKey;
			})
			.orElse(null);

		counselingSessionRepository.delete(counselingSession);

		return recordingKey;
	}
}
