package com.ktb.hackathon.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
	String refreshCookieName,
	String refreshCookiePath,
	boolean refreshCookieSecure,
	String refreshCookieSameSite,
	List<String> corsAllowedOrigin
) {
}
