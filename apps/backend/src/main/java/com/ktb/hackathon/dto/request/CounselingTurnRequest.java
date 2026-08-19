package com.ktb.hackathon.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CounselingTurnRequest(
	@NotBlank(message = "role은 필수입니다.")
	String role,
	@NotBlank(message = "turn의 text는 필수입니다.")
	String text,
	String itemId,
	String status
) {
}
