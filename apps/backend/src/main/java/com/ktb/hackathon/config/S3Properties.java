package com.ktb.hackathon.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
	String region,
	String bucket,
	String profileImagePrefix,
	DataSize maxFileSize,
	Duration presignedUrlExpiration
) {
}
