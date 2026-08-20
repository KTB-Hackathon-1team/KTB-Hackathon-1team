package com.ktb.hackathon.service;

import com.ktb.hackathon.auth.JwtTokenProvider;
import com.ktb.hackathon.dto.request.LoginRequest;
import com.ktb.hackathon.dto.request.SignUpRequest;
import com.ktb.hackathon.entity.ParentAccount;
import com.ktb.hackathon.entity.RefreshToken;
import com.ktb.hackathon.exception.AuthException;
import com.ktb.hackathon.repository.ParentAccountRepository;
import com.ktb.hackathon.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {

	private final ParentAccountRepository parentAccountRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final SecureRandom secureRandom = new SecureRandom();

	public AuthService(
		ParentAccountRepository parentAccountRepository,
		RefreshTokenRepository refreshTokenRepository,
		PasswordEncoder passwordEncoder,
		JwtTokenProvider jwtTokenProvider
	) {
		this.parentAccountRepository = parentAccountRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Transactional
	public AuthResult signUp(SignUpRequest request) {
		if (parentAccountRepository.existsByLoginId(request.loginId())) {
			throw new AuthException(
				HttpStatus.CONFLICT,
				"DUPLICATE_LOGIN_ID",
				"이미 사용 중인 loginId입니다."
			);
		}

		ParentAccount account = ParentAccount.builder()
			.loginId(request.loginId())
			.passwordHash(passwordEncoder.encode(request.password()))
			.nickname(request.nickname())
			.build();
		parentAccountRepository.save(account);

		return issueTokens(account);
	}

	@Transactional
	public AuthResult login(LoginRequest request) {
		ParentAccount account = parentAccountRepository.findByLoginId(request.loginId())
			.orElseThrow(this::invalidCredentials);

		if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
			throw invalidCredentials();
		}

		return issueTokens(account);
	}

	@Transactional
	public AuthResult refresh(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw invalidRefreshToken();
		}

		String tokenHash = hashToken(rawRefreshToken);
		RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
			.orElseThrow(this::invalidRefreshToken);

		if (!refreshToken.getExpiresAt().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
			throw invalidRefreshToken();
		}

		ParentAccount account = refreshToken.getParentAccount();
		return new AuthResult(jwtTokenProvider.createAccessToken(account), account, null);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}

		refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
			.ifPresent(refreshTokenRepository::delete);
	}

	private AuthResult issueTokens(ParentAccount account) {
		String rawRefreshToken = generateRefreshToken();
		replaceRefreshToken(account, rawRefreshToken);
		return new AuthResult(
			jwtTokenProvider.createAccessToken(account),
			account,
			rawRefreshToken
		);
	}

	private void replaceRefreshToken(ParentAccount account, String rawRefreshToken) {
		refreshTokenRepository.deleteByParentAccountId(account.getId());
		refreshTokenRepository.flush();

		RefreshToken refreshToken = RefreshToken.builder()
			.parentAccount(account)
			.tokenHash(hashToken(rawRefreshToken))
			.expiresAt(LocalDateTime.now(ZoneOffset.UTC)
				.plus(jwtTokenProvider.refreshTokenExpiration()))
			.build();
		refreshTokenRepository.save(refreshToken);
	}

	private String generateRefreshToken() {
		byte[] tokenBytes = new byte[32];
		secureRandom.nextBytes(tokenBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
	}

	private String hashToken(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
				.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
		}
	}

	private AuthException invalidCredentials() {
		return new AuthException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_CREDENTIALS",
			"아이디 또는 비밀번호가 올바르지 않습니다."
		);
	}

	private AuthException invalidRefreshToken() {
		return new AuthException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_REFRESH_TOKEN",
			"Refresh Token이 유효하지 않거나 만료되었습니다."
		);
	}

	public record AuthResult(
		String accessToken,
		ParentAccount account,
		String refreshToken
	) {
	}
}
