package com.ktb.hackathon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "child_profiles",
	indexes = @Index(name = "idx_child_profile_parent", columnList = "parent_account_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChildProfile extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "parent_account_id", nullable = false)
	private ParentAccount parentAccount;

	@Column(nullable = false, length = 100)
	private String nickname;

	@Column(name = "birth_year", nullable = false)
	private Integer birthYear;

	@Builder
	private ChildProfile(ParentAccount parentAccount, String nickname, Integer birthYear) {
		this.parentAccount = parentAccount;
		this.nickname = nickname;
		this.birthYear = birthYear;
	}
}
