package com.ktb.hackathon.dto.response;

import com.ktb.hackathon.entity.ChildProfile;
import com.ktb.hackathon.entity.enums.ChildGender;

import java.time.LocalDate;

public record ChildProfileResponse(
	Long id,
	String name,
	LocalDate birthDate,
	ChildGender gender,
	String profileImageUrl
) {

	public static ChildProfileResponse from(ChildProfile childProfile, String profileImageUrl) {
		return new ChildProfileResponse(
			childProfile.getId(),
			childProfile.getName(),
			childProfile.getBirthDate(),
			childProfile.getGender(),
			profileImageUrl
		);
	}
}
