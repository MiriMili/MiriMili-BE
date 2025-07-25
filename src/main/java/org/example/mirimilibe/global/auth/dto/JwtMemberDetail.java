package org.example.mirimilibe.global.auth.dto;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtMemberDetail extends User {
	private final Long memberId;

	@Builder(builderMethodName = "jwtMemberDetailBuilder")
	public JwtMemberDetail(Long memberId, String username, String password,
		Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.memberId = memberId;
	}

}
