package com.ktb.hackathon.controller;

import com.ktb.hackathon.auth.AuthenticatedUser;
import com.ktb.hackathon.dto.response.CommonResponse;
import com.ktb.hackathon.dto.response.RealtimeClientSecretResponse;
import com.ktb.hackathon.service.VoiceRealtimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

	private final VoiceRealtimeService voiceRealtimeService;

	public VoiceController(VoiceRealtimeService voiceRealtimeService) {
		this.voiceRealtimeService = voiceRealtimeService;
	}

	@PostMapping("/realtime-token")
	public ResponseEntity<CommonResponse<RealtimeClientSecretResponse>> issueRealtimeToken(
		@AuthenticationPrincipal AuthenticatedUser authenticatedUser
	) {
		RealtimeClientSecretResponse response = voiceRealtimeService.issueClientSecret();
		return ResponseEntity.ok(CommonResponse.of("Realtime 임시 키 발급 성공", response));
	}
}
