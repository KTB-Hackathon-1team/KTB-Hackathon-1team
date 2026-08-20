package com.ktb.hackathon.auth;

import com.ktb.hackathon.entity.enums.AccountRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			if (request.getRequestURI().startsWith("/api/voice")) {
				log.warn(
					"JWT 인증 헤더 없음 method={}, uri={}, origin={}",
					request.getMethod(),
					request.getRequestURI(),
					request.getHeader("Origin")
				);
			}
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorization.substring(BEARER_PREFIX.length());
		try {
			Jwt jwt = jwtTokenProvider.decode(token);
			AuthenticatedUser user = new AuthenticatedUser(
				Long.valueOf(jwt.getSubject()),
				AccountRole.valueOf(jwt.getClaimAsString("role"))
			);

			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(user, null, user.authorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.info(
				"JWT 인증 성공 method={}, uri={}, subject={}, role={}, origin={}",
				request.getMethod(),
				request.getRequestURI(),
				jwt.getSubject(),
				jwt.getClaimAsString("role"),
				request.getHeader("Origin")
			);
		} catch (JwtException | IllegalArgumentException exception) {
			log.warn(
				"JWT 인증 실패 method={}, uri={}, reason={}, origin={}",
				request.getMethod(),
				request.getRequestURI(),
				exception.getClass().getSimpleName(),
				request.getHeader("Origin")
			);
			SecurityContextHolder.clearContext();
			if (!request.getRequestURI().startsWith("/api/auth/")) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 Access Token입니다.");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}
