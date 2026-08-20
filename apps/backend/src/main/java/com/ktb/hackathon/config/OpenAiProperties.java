package com.ktb.hackathon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public record OpenAiProperties(
	String apiKey,
	String realtimeClientSecretsUrl
) {
}
