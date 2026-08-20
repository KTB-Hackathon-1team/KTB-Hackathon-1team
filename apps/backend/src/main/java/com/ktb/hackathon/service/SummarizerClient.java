package com.ktb.hackathon.service;

import com.ktb.hackathon.config.SummarizerProperties;
import com.ktb.hackathon.dto.request.SummarizeRequest;
import com.ktb.hackathon.dto.response.SummarizeResponse;
import com.ktb.hackathon.exception.SummarizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;

@Service
public class SummarizerClient {

	private static final Logger log = LoggerFactory.getLogger(SummarizerClient.class);

	private final RestClient restClient;
	private final SummarizerProperties properties;

	public SummarizerClient(
		@Qualifier("summarizerRestClient") RestClient summarizerRestClient,
		SummarizerProperties properties
	) {
		this.restClient = summarizerRestClient;
		this.properties = properties;
	}

	public SummarizeResponse summarize(Long sessionId) {
		SummarizeResponse response;
		URI uri = summarizeUri();
		try {
			response = restClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(new SummarizeRequest(sessionId))
				.retrieve()
				.body(SummarizeResponse.class);
		} catch (RestClientResponseException exception) {
			log.error(
				"요약 서버 HTTP 오류: uri={}, status={}, responseBody={}",
				uri,
				exception.getStatusCode(),
				exception.getResponseBodyAsString(),
				exception
			);
			throw new SummarizationException(
				HttpStatus.BAD_GATEWAY,
				"상담 요약 서버가 요청을 처리하지 못했습니다."
			);
		} catch (RestClientException exception) {
			log.error("요약 서버 연결 실패: uri={}", uri, exception);
			throw new SummarizationException(
				HttpStatus.BAD_GATEWAY,
				"상담 요약 서버에 연결할 수 없습니다."
			);
		}

		if (response == null
			|| !sessionId.equals(response.sessionId())
			|| !StringUtils.hasText(response.summary())) {
			throw new SummarizationException(
				HttpStatus.BAD_GATEWAY,
				"상담 요약 서버 응답이 올바르지 않습니다."
			);
		}

		return response;
	}

	private URI summarizeUri() {
		String baseUrl = properties.baseUrl().replaceAll("/+$", "");
		return URI.create(baseUrl + "/summarize");
	}
}
