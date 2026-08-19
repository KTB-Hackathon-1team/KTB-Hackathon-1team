package com.ktb.hackathon.auth;

import com.ktb.hackathon.entity.enums.AccountRole;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthenticatedUser(
	Long parentAccountId,
	AccountRole role
) {

	public Collection<? extends GrantedAuthority> authorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}
}
