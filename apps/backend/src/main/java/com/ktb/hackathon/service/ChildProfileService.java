package com.ktb.hackathon.service;

import com.ktb.hackathon.auth.AuthenticatedUser;
import com.ktb.hackathon.dto.request.ChildProfileCreateRequest;
import com.ktb.hackathon.dto.response.ChildProfileResponse;
import com.ktb.hackathon.entity.ChildProfile;
import com.ktb.hackathon.entity.ParentAccount;
import com.ktb.hackathon.exception.AuthException;
import com.ktb.hackathon.repository.ChildProfileRepository;
import com.ktb.hackathon.repository.ParentAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ChildProfileService {

	private final ChildProfileRepository childProfileRepository;
	private final ParentAccountRepository parentAccountRepository;
	private final S3ImageService s3ImageService;

	public ChildProfileService(
		ChildProfileRepository childProfileRepository,
		ParentAccountRepository parentAccountRepository,
		S3ImageService s3ImageService
	) {
		this.childProfileRepository = childProfileRepository;
		this.parentAccountRepository = parentAccountRepository;
		this.s3ImageService = s3ImageService;
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
