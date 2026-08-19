package com.ktb.hackathon.dto.response;

import com.ktb.hackathon.entity.ParentAccount;
import com.ktb.hackathon.entity.enums.AccountRole;

public record UserResponse(
	Long id,
	String loginId,
	String nickname,
	AccountRole role
) {

	public static UserResponse from(ParentAccount account) {
		return new UserResponse(
			account.getId(),
			account.getLoginId(),
			account.getNickname(),
			account.getRole()
		);
	}
}
