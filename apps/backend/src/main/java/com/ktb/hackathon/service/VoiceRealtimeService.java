package com.ktb.hackathon.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.ktb.hackathon.config.OpenAiProperties;
import com.ktb.hackathon.dto.response.RealtimeClientSecretResponse;
import com.ktb.hackathon.exception.VoiceException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class VoiceRealtimeService {

	private static final String SESSION_CONFIG_PATH = "voice/child-agent-session.json";

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;
	private final JsonNode sessionConfig;

	public VoiceRealtimeService(
		RestClient openAiRestClient,
		OpenAiProperties properties,
		ObjectMapper objectMapper
	) {
		this.openAiRestClient = openAiRestClient;
		this.properties = properties;
		this.sessionConfig = loadSessionConfig(objectMapper);
	}

	public RealtimeClientSecretResponse issueClientSecret() {
		if (!StringUtils.hasText(properties.apiKey())) {
			throw new VoiceException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"백엔드 OPENAI_API_KEY가 설정되지 않았습니다."
			);
		}

		OpenAiClientSecretResponse response;
		try {
			response = openAiRestClient.post()
				.uri(properties.realtimeClientSecretsUrl())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
				.contentType(MediaType.APPLICATION_JSON)
				.body(sessionConfig)
				.retrieve()
				.body(OpenAiClientSecretResponse.class);
		} catch (RestClientResponseException exception) {
			throw new VoiceException(
				HttpStatus.BAD_GATEWAY,
				"OpenAI Realtime 임시 키 발급에 실패했습니다."
			);
		} catch (RestClientException exception) {
			throw new VoiceException(
				HttpStatus.BAD_GATEWAY,
				"OpenAI Realtime 서버에 연결할 수 없습니다."
			);
		}

		if (response == null || !StringUtils.hasText(response.value())) {
			throw new VoiceException(
				HttpStatus.BAD_GATEWAY,
				"OpenAI Realtime 임시 키 응답이 올바르지 않습니다."
			);
		}

		return new RealtimeClientSecretResponse(response.value(), response.expiresAt());
	}

	private JsonNode loadSessionConfig(ObjectMapper objectMapper) {
		ClassPathResource resource = new ClassPathResource(SESSION_CONFIG_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			return objectMapper.readTree(inputStream);
		} catch (IOException exception) {
			throw new IllegalStateException("Realtime 세션 설정을 불러올 수 없습니다.", exception);
		}
	}

	private record OpenAiClientSecretResponse(
		String value,
		@JsonProperty("expires_at") Long expiresAt
	) {
	}
}
