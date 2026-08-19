package com.ktb.hackathon.service;

import com.ktb.hackathon.auth.AuthenticatedUser;
import com.ktb.hackathon.dto.request.ChildProfileCreateRequest;
import com.ktb.hackathon.dto.response.ChildProfileResponse;
import com.ktb.hackathon.entity.ChildProfile;
import com.ktb.hackathon.entity.CounselingSession;
import com.ktb.hackathon.entity.ParentAccount;
import com.ktb.hackathon.exception.AuthException;
import com.ktb.hackathon.repository.ChildProfileRepository;
import com.ktb.hackathon.repository.CounselingSessionRepository;
import com.ktb.hackathon.repository.ParentAccountRepository;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ChildProfileService {

	private final ChildProfileRepository childProfileRepository;
	private final ParentAccountRepository parentAccountRepository;
	private final S3ImageService s3ImageService;
	private final CounselingSessionRepository counselingSessionRepository;
	private final CounselingSessionCleanupService counselingSessionCleanupService;

	public ChildProfileService(
		ChildProfileRepository childProfileRepository,
		ParentAccountRepository parentAccountRepository,
		S3ImageService s3ImageService,
		CounselingSessionRepository counselingSessionRepository,
		CounselingSessionCleanupService counselingSessionCleanupService
	) {
		this.childProfileRepository = childProfileRepository;
		this.parentAccountRepository = parentAccountRepository;
		this.s3ImageService = s3ImageService;
		this.counselingSessionRepository = counselingSessionRepository;
		this.counselingSessionCleanupService = counselingSessionCleanupService;
	}

	@Transactional
	public ChildProfileResponse create(
		AuthenticatedUser authenticatedUser,
		ChildProfileCreateRequest request
	) {
		ParentAccount parentAccount = parentAccountRepository.findById(authenticatedUser.parentAccountId())
			.orElseThrow(() -> new AuthException(
				HttpStatus.UNAUTHORIZED,
				"PARENT_ACCOUNT_NOT_FOUND",
				"인증된 부모 계정을 찾을 수 없습니다."
			));

		ChildProfile childProfile = ChildProfile.builder()
			.parentAccount(parentAccount)
			.name(request.name())
			.birthDate(request.birthDate())
			.gender(request.gender())
			.build();

		return toResponse(childProfileRepository.save(childProfile));
	}

	@Transactional
	public ChildProfileResponse update(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		ChildProfileCreateRequest request
	) {
		ChildProfile childProfile = findOwnedChildProfile(authenticatedUser, childProfileId);
		childProfile.updateProfile(request.name(), request.birthDate(), request.gender());
		return toResponse(childProfile);
	}

	@Transactional(readOnly = true)
	public List<ChildProfileResponse> findAll(AuthenticatedUser authenticatedUser) {
		return childProfileRepository.findAllByParentAccountIdOrderByCreatedAtAsc(
			authenticatedUser.parentAccountId()
		).stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public ChildProfileResponse uploadProfileImage(
		AuthenticatedUser authenticatedUser,
		Long childProfileId,
		MultipartFile file
	) {
		ChildProfile childProfile = findOwnedChildProfile(authenticatedUser, childProfileId);
		String previousImageKey = childProfile.getProfileImageKey();
		String newImageKey = s3ImageService.uploadProfileImage(
			authenticatedUser.parentAccountId(),
			childProfileId,
			file
		);

		childProfile.updateProfileImageKey(newImageKey);
		ChildProfileResponse response = toResponse(childProfile);
		s3ImageService.deleteQuietly(previousImageKey);
		return response;
	}

	@Transactional
	public void delete(AuthenticatedUser authenticatedUser, Long childProfileId) {
		ChildProfile childProfile = findOwnedChildProfile(authenticatedUser, childProfileId);
		List<CounselingSession> counselingSessions = counselingSessionRepository
			.findAllByChildProfileIdAndChildProfileParentAccountIdOrderByIdDesc(
				childProfileId,
				authenticatedUser.parentAccountId()
			);
		List<String> recordingKeys = new ArrayList<>();

		for (CounselingSession counselingSession : counselingSessions) {
			String recordingKey = counselingSessionCleanupService.delete(counselingSession);
			if (recordingKey != null) {
				recordingKeys.add(recordingKey);
			}
		}

		counselingSessionRepository.flush();
		childProfileRepository.delete(childProfile);
		childProfileRepository.flush();

		s3ImageService.deleteQuietly(childProfile.getProfileImageKey());
		for (String recordingKey : recordingKeys) {
			s3ImageService.deleteQuietly(recordingKey);
		}
	}

	private ChildProfile findOwnedChildProfile(AuthenticatedUser authenticatedUser, Long childProfileId) {
		return childProfileRepository.findByIdAndParentAccountId(
			childProfileId,
			authenticatedUser.parentAccountId()
		).orElseThrow(() -> new AuthException(
			HttpStatus.NOT_FOUND,
			"CHILD_PROFILE_NOT_FOUND",
			"아이 프로필을 찾을 수 없습니다."
		));
	}

	private ChildProfileResponse toResponse(ChildProfile childProfile) {
		return ChildProfileResponse.from(
			childProfile,
			s3ImageService.createReadUrl(childProfile.getProfileImageKey())
		);
	}
}
