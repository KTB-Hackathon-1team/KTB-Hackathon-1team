package com.ktb.hackathon.exception;

import org.springframework.http.HttpStatus;

public class VoiceException extends RuntimeException {

	private final HttpStatus status;

	public VoiceException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
