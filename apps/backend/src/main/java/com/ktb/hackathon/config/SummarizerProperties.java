package com.ktb.hackathon.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.summarizer")
public record SummarizerProperties(
	String baseUrl,
	Duration connectTimeout,
	Duration readTimeout
) {
}
