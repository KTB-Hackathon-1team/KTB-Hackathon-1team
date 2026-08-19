package com.ktb.hackathon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "refresh_tokens",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_refresh_token_parent", columnNames = "parent_account_id"),
		@UniqueConstraint(name = "uk_refresh_token_hash", columnNames = "token_hash")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "parent_account_id", nullable = false, unique = true)
	private ParentAccount parentAccount;

	@Column(name = "token_hash", nullable = false, length = 64, unique = true)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Builder
	private RefreshToken(ParentAccount parentAccount, String tokenHash, LocalDateTime expiresAt) {
		this.parentAccount = parentAccount;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}
}
