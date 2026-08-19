package com.ktb.hackathon.controller;

import com.ktb.hackathon.auth.AuthenticatedUser;
import com.ktb.hackathon.dto.request.ChildProfileCreateRequest;
import com.ktb.hackathon.dto.response.ChildProfileResponse;
import com.ktb.hackathon.dto.response.CommonResponse;
import com.ktb.hackathon.service.ChildProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/children")
public class ChildProfileController {

	private final ChildProfileService childProfileService;

	public ChildProfileController(ChildProfileService childProfileService) {
		this.childProfileService = childProfileService;
	}

	@GetMapping
	public ResponseEntity<CommonResponse<List<ChildProfileResponse>>> findAll(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser
	) {
		List<ChildProfileResponse> childProfiles = childProfileService.findAll(authenticatedUser);
		return ResponseEntity.ok(CommonResponse.of("아이 프로필 목록 조회 성공", childProfiles));
	}

	@PostMapping
	public ResponseEntity<CommonResponse<ChildProfileResponse>> create(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@Valid @RequestBody ChildProfileCreateRequest request
	) {
		ChildProfileResponse childProfile = childProfileService.create(authenticatedUser, request);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(CommonResponse.of("아이 프로필 생성 성공", childProfile));
	}

	@PutMapping("/{childProfileId}")
	public ResponseEntity<CommonResponse<ChildProfileResponse>> update(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@PathVariable Long childProfileId,
		@Valid @RequestBody ChildProfileCreateRequest request
	) {
		ChildProfileResponse childProfile = childProfileService.update(
			authenticatedUser,
			childProfileId,
			request
		);
		return ResponseEntity.ok(CommonResponse.of("아이 프로필 수정 성공", childProfile));
	}

	@DeleteMapping("/{childProfileId}")
	public ResponseEntity<CommonResponse<Void>> delete(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@PathVariable Long childProfileId
	) {
		childProfileService.delete(authenticatedUser, childProfileId);
		return ResponseEntity.ok(CommonResponse.of("아이 프로필 삭제 성공", null));
	}

	@PostMapping(value = "/{childProfileId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CommonResponse<ChildProfileResponse>> uploadProfileImage(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@PathVariable Long childProfileId,
		@RequestPart("file") MultipartFile file
	) {
		ChildProfileResponse childProfile = childProfileService.uploadProfileImage(
			authenticatedUser,
			childProfileId,
			file
		);
		return ResponseEntity.ok(CommonResponse.of("프로필 이미지 업로드 성공", childProfile));
	}
}
