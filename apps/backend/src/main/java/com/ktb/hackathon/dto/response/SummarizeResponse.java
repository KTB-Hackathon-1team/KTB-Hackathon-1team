package com.ktb.hackathon.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SummarizeResponse(
	@JsonProperty("session_id") Long sessionId,
	String summary
) {
}
