package com.ktb.hackathon.exception;

import org.springframework.http.HttpStatus;

public class ImageStorageException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	public ImageStorageException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}
}
