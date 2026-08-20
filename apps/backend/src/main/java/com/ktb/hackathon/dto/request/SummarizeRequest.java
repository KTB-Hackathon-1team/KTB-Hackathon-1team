package com.ktb.hackathon.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SummarizeRequest(
	@JsonProperty("session_id") Long sessionId
) {
}
