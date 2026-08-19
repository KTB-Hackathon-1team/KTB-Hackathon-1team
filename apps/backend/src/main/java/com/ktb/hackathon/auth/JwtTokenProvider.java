package com.ktb.hackathon.auth;

import com.ktb.hackathon.entity.ParentAccount;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private static final String ISSUER = "ktb-hackathon";

	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final JwtProperties properties;

	public JwtTokenProvider(
		JwtEncoder jwtEncoder,
		JwtDecoder jwtDecoder,
		JwtProperties properties
	) {
		this.jwtEncoder = jwtEncoder;
		this.jwtDecoder = jwtDecoder;
		this.properties = properties;
	}

	public String createAccessToken(ParentAccount account) {
		Instant issuedAt = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(ISSUER)
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plus(properties.accessTokenExpiration()))
			.subject(account.getId().toString())
			.claim("role", account.getRole().name())
			.claim("loginId", account.getLoginId())
			.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public Jwt decode(String token) {
		return jwtDecoder.decode(token);
	}

	public Duration refreshTokenExpiration() {
		return properties.refreshTokenExpiration();
	}
}
