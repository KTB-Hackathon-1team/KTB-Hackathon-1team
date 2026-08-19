package com.ktb.hackathon.controller;

import com.ktb.hackathon.auth.AuthProperties;
import com.ktb.hackathon.auth.JwtProperties;
import com.ktb.hackathon.dto.request.LoginRequest;
import com.ktb.hackathon.dto.request.SignUpRequest;
import com.ktb.hackathon.dto.response.AuthResponse;
import com.ktb.hackathon.dto.response.CommonResponse;
import com.ktb.hackathon.dto.response.UserResponse;
import com.ktb.hackathon.exception.AuthException;
import com.ktb.hackathon.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final JwtProperties jwtProperties;
	private final AuthProperties authProperties;

	public AuthController(
		AuthService authService,
		JwtProperties jwtProperties,
		AuthProperties authProperties
	) {
		this.authService = authService;
		this.jwtProperties = jwtProperties;
		this.authProperties = authProperties;
	}

	@PostMapping("/signup")
	public ResponseEntity<CommonResponse<AuthResponse>> signUp(
		@Valid @RequestBody SignUpRequest request,
		HttpServletResponse response
	) {
		AuthService.AuthResult result = authService.signUp(request);
		addRefreshCookie(response, result.refreshToken());
		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.of("회원가입 성공", toResponse(result)));
	}

	@PostMapping("/login")
	public ResponseEntity<CommonResponse<AuthResponse>> login(
		@Valid @RequestBody LoginRequest request,
		HttpServletResponse response
	) {
		AuthService.AuthResult result = authService.login(request);
		addRefreshCookie(response, result.refreshToken());
		return ResponseEntity.ok(CommonResponse.of("로그인 성공", toResponse(result)));
	}

	@PostMapping("/refresh")
	public ResponseEntity<CommonResponse<AuthResponse>> refresh(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		try {
			AuthService.AuthResult result = authService.refresh(readRefreshToken(request));
			return ResponseEntity.ok(CommonResponse.of("refresh token 성공", toResponse(result)));
		} catch (AuthException exception) {
			clearRefreshCookie(response);
			throw exception;
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<CommonResponse<Void>> logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		authService.logout(readRefreshToken(request));
		clearRefreshCookie(response);
		return ResponseEntity.ok(CommonResponse.<Void>of("로그아웃 성공", null));
	}

	private AuthResponse toResponse(AuthService.AuthResult result) {
		return new AuthResponse(
			result.accessToken(),
			"Bearer",
			jwtProperties.accessTokenExpiration().toSeconds(),
			UserResponse.from(result.account())
		);
	}

	private String readRefreshToken(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, authProperties.refreshCookieName());
		return cookie == null ? null : cookie.getValue();
	}

	private void addRefreshCookie(HttpServletResponse response, String rawRefreshToken) {
		ResponseCookie cookie = ResponseCookie.from(authProperties.refreshCookieName(), rawRefreshToken)
			.httpOnly(true)
			.secure(authProperties.refreshCookieSecure())
			.path(authProperties.refreshCookiePath())
			.sameSite(authProperties.refreshCookieSameSite())
			.maxAge(jwtProperties.refreshTokenExpiration())
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void clearRefreshCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(authProperties.refreshCookieName(), "")
			.httpOnly(true)
			.secure(authProperties.refreshCookieSecure())
			.path(authProperties.refreshCookiePath())
			.sameSite(authProperties.refreshCookieSameSite())
			.maxAge(Duration.ZERO)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
