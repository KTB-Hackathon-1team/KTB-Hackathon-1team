package com.ktb.hackathon.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CounselingHandoffRequest(
	@NotEmpty(message = "turns는 한 개 이상이어야 합니다.")
	List<@Valid CounselingTurnRequest> turns,
	@NotBlank(message = "text는 필수입니다.")
	String text
) {
}
