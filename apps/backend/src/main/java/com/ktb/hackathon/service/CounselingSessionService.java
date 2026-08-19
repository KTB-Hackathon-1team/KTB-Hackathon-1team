package com.ktb.hackathon.service;

import com.ktb.hackathon.auth.AuthenticatedUser;
import com.ktb.hackathon.dto.request.CounselingSessionCreateRequest;
import com.ktb.hackathon.dto.response.CounselingSessionResponse;
import com.ktb.hackathon.dto.response.CounselingSessionListResponse;
import com.ktb.hackathon.entity.ChildProfile;
import com.ktb.hackathon.entity.CounselingSession;
import com.ktb.hackathon.exception.AuthException;
import com.ktb.hackathon.repository.ChildProfileRepository;
import com.ktb.hackathon.repository.CounselingSessionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CounselingSessionService {

	private static final int MAX_PAGE_SIZE = 5;

	private final CounselingSessionRepository counselingSessionRepository;
	private final ChildProfileRepository childProfileRepository;

	public CounselingSessionService(
		CounselingSessionRepository counselingSessionRepository,
		ChildProfileRepository childProfileRepository
	) {
		this.counselingSessionRepository = counselingSessionRepository;
		this.childProfileRepository = childProfileRepository;
	}

	@Transactional
	public CounselingSessionResponse create(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		CounselingSessionCreateRequest request
	) {
		ChildProfile childProfile = findOwnedChildProfile(authenticatedUser, childProfileId);

		CounselingSession counselingSession = CounselingSession.builder()
			.childProfile(childProfile)
			.title(request.title())
			.content(request.content())
			.build();

		return CounselingSessionResponse.from(counselingSessionRepository.save(counselingSession));
	}

	public CounselingSessionListResponse findAll(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		Long cursorId,
		int size
	) {
		findOwnedChildProfile(authenticatedUser, childProfileId);
		validateCursor(cursorId);
		validatePageSize(size);

		int fetchSize = size + 1;
		List<CounselingSession> sessions = cursorId == null
			? counselingSessionRepository.findAllByChildProfileIdAndChildProfileParentAccountIdOrderByIdDesc(
				childProfileId,
				authenticatedUser.parentAccountId(),
				PageRequest.of(0, fetchSize)
			)
			: counselingSessionRepository.findAllByChildProfileIdAndChildProfileParentAccountIdAndIdLessThanOrderByIdDesc(
				childProfileId,
				authenticatedUser.parentAccountId(),
				cursorId,
				PageRequest.of(0, fetchSize)
			);

		boolean hasNext = sessions.size() > size;
		List<CounselingSession> visibleSessions = hasNext
			? new ArrayList<>(sessions.subList(0, size))
			: sessions;

		List<CounselingSessionResponse> items = visibleSessions.stream()
			.map(CounselingSessionResponse::from)
			.toList();

		Long nextCursorId = hasNext && !visibleSessions.isEmpty()
			? visibleSessions.get(visibleSessions.size() - 1).getId()
			: null;

		return new CounselingSessionListResponse(items, nextCursorId, hasNext);
	}

	private ChildProfile findOwnedChildProfile(
		AuthenticatedUser authenticatedUser,
		Long childProfileId
	) {
		return childProfileRepository.findByIdAndParentAccountId(
			childProfileId,
			authenticatedUser.parentAccountId()
		).orElseThrow(() -> new AuthException(
			HttpStatus.NOT_FOUND,
			"CHILD_PROFILE_NOT_FOUND",
			"아이 프로필을 찾을 수 없습니다."
		));
	}

	private void validateCursor(Long cursorId) {
		if (cursorId != null && cursorId <= 0) {
			throw new AuthException(
				HttpStatus.BAD_REQUEST,
				"INVALID_CURSOR",
				"cursorId는 1 이상이어야 합니다."
			);
		}
	}

	private void validatePageSize(int size) {
		if (size < 1 || size > MAX_PAGE_SIZE) {
			throw new AuthException(
				HttpStatus.BAD_REQUEST,
				"INVALID_PAGE_SIZE",
				"size는 1 이상 5 이하여야 합니다."
			);
		}
	}
}
