package com.ktb.hackathon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CounselingSessionCreateRequest(
	@NotBlank(message = "title은 필수입니다.")
	@Size(max = 200, message = "title은 200자 이하여야 합니다.")
	String title,
	@NotBlank(message = "content는 필수입니다.")
	String content
) {
}
