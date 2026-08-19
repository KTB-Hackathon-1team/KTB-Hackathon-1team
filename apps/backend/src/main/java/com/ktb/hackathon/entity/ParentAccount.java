package com.ktb.hackathon.entity;

import com.ktb.hackathon.entity.enums.AccountRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "parent_accounts",
	uniqueConstraints = @UniqueConstraint(name = "uk_parent_account_login_id", columnNames = "login_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentAccount extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "login_id", nullable = false, length = 100)
	private String loginId;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(nullable = false, length = 100)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AccountRole role;

	@Builder
	private ParentAccount(String loginId, String passwordHash, String nickname) {
		this.loginId = loginId;
		this.passwordHash = passwordHash;
		this.nickname = nickname;
		this.role = AccountRole.PARENT;
	}
}
