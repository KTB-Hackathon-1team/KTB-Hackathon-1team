package com.ktb.hackathon.controller;

import com.ktb.hackathon.dto.response.CommonResponse;
import com.ktb.hackathon.exception.AuthException;
import com.ktb.hackathon.exception.ImageStorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

	@ExceptionHandler(AuthException.class)
	public ResponseEntity<CommonResponse<Void>> handleAuthException(AuthException exception) {
		return ResponseEntity.status(exception.getStatus())
			.body(CommonResponse.<Void>of(exception.getMessage(), null));
	}

	@ExceptionHandler(ImageStorageException.class)
	public ResponseEntity<CommonResponse<Void>> handleImageStorageException(ImageStorageException exception) {
		return ResponseEntity.status(exception.getStatus())
			.body(CommonResponse.<Void>of(exception.getMessage(), null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(fieldError -> fieldError.getDefaultMessage())
			.orElse("요청 값이 올바르지 않습니다.");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(CommonResponse.<Void>of(message, null));
	}
}
