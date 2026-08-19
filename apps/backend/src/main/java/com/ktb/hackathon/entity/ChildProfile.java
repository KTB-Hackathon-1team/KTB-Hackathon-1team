package com.ktb.hackathon.entity;

import com.ktb.hackathon.entity.enums.ChildGender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDate;

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
	private String name;

	@Column(name = "birth_date", nullable = false)
	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ChildGender gender;

	@Column(name = "profile_image_key", length = 500)
	private String profileImageKey;

	@Builder
	private ChildProfile(ParentAccount parentAccount, String name, LocalDate birthDate, ChildGender gender) {
		this.parentAccount = parentAccount;
		this.name = name;
		this.birthDate = birthDate;
		this.gender = gender;
	}

	public void updateProfileImageKey(String profileImageKey) {
		this.profileImageKey = profileImageKey;
	}
}
