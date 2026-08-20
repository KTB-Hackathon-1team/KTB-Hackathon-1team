package com.ktb.hackathon.dto.response;

public record RealtimeClientSecretResponse(
	String clientSecret,
	Long expiresAt
) {
}
