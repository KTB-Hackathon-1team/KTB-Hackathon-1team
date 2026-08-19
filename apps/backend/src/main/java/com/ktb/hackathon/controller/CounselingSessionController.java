package com.ktb.hackathon.controller;

import com.ktb.hackathon.auth.AuthenticatedUser;
import com.ktb.hackathon.dto.request.CounselingSessionCreateRequest;
import com.ktb.hackathon.dto.response.CommonResponse;
import com.ktb.hackathon.dto.response.CounselingSessionDetailResponse;
import com.ktb.hackathon.dto.response.CounselingSessionListResponse;
import com.ktb.hackathon.dto.response.CounselingSessionResponse;
import com.ktb.hackathon.service.CounselingSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/children/{childProfileId}/counseling-sessions")
public class CounselingSessionController {

	private final CounselingSessionService counselingSessionService;

	public CounselingSessionController(CounselingSessionService counselingSessionService) {
		this.counselingSessionService = counselingSessionService;
	}

	@PostMapping
	public ResponseEntity<CommonResponse<CounselingSessionResponse>> create(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@PathVariable Long childProfileId,
		@Valid @RequestBody CounselingSessionCreateRequest request
	) {
		CounselingSessionResponse response = counselingSessionService.create(
			authenticatedUser,
			childProfileId,
			request
		);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(CommonResponse.of("상담 상황 생성 성공", response));
	}

	@GetMapping
	public ResponseEntity<CommonResponse<CounselingSessionListResponse>> findAll(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@PathVariable Long childProfileId,
		@RequestParam(required = false) Long cursorId,
		@RequestParam(defaultValue = "5") int size
	) {
		CounselingSessionListResponse response = counselingSessionService.findAll(
			authenticatedUser,
			childProfileId,
			cursorId,
			size
		);

		return ResponseEntity.ok(CommonResponse.of("상담 기록 조회 성공", response));
	}

	@GetMapping("/{sessionId}")
	public ResponseEntity<CommonResponse<CounselingSessionDetailResponse>> findDetail(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@PathVariable Long childProfileId,
		@PathVariable Long sessionId
	) {
		CounselingSessionDetailResponse response = counselingSessionService.findDetail(
			authenticatedUser,
			childProfileId,
			sessionId
		);

		return ResponseEntity.ok(CommonResponse.of("상담 세션 상세 조회 성공", response));
	}

	@PostMapping("/{sessionId}/start")
	public ResponseEntity<CommonResponse<CounselingSessionDetailResponse>> startRecording(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
		@PathVariable Long childProfileId,
		@PathVariable Long sessionId
	) {
		CounselingSessionDetailResponse response = counselingSessionService.startRecording(
			authenticatedUser,
			childProfileId,
			sessionId
		);

		return ResponseEntity.ok(CommonResponse.of("녹음 시작 준비 성공", response));
	}
}
