package com.ktb.hackathon.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ktb.hackathon.config.SummarizerProperties;
import com.ktb.hackathon.dto.response.SummarizeResponse;
import com.ktb.hackathon.exception.SummarizationException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class SummarizerClientTest {

	private HttpServer server;
	private AtomicReference<String> requestBody;
	private AtomicReference<String> responseBody;

	@BeforeEach
	void setUp() throws IOException {
		requestBody = new AtomicReference<>();
		responseBody = new AtomicReference<>("{\"session_id\":42,\"summary\":\"요약 결과\"}");
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/summarize", exchange -> {
			requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] response = responseBody.get().getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length);
			try (OutputStream outputStream = exchange.getResponseBody()) {
				outputStream.write(response);
			}
		});
		server.start();
	}

	@AfterEach
	void tearDown() {
		server.stop(0);
	}

	@Test
	void sendsSessionIdInJsonBodyAndReadsSummaryResponse() {
		SummarizerClient client = client();

		SummarizeResponse response = client.summarize(42L);

		assertEquals(42L, response.sessionId());
		assertEquals("요약 결과", response.summary());
		assertTrue(requestBody.get().contains("\"session_id\":42"));
	}

	@Test
	void rejectsResponseForAnotherSession() {
		responseBody.set("{\"session_id\":99,\"summary\":\"다른 세션\"}");
		SummarizerClient client = client();

		assertThrows(SummarizationException.class, () -> client.summarize(42L));
	}

	private SummarizerClient client() {
		String baseUrl = URI.create("http://127.0.0.1:" + server.getAddress().getPort()).toString();
		SummarizerProperties properties = new SummarizerProperties(
			baseUrl,
			Duration.ofSeconds(1),
			Duration.ofSeconds(1)
		);
		RestClient restClient = RestClient.builder()
			.requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build()
			))
			.build();
		return new SummarizerClient(restClient, properties);
	}
}
