package org.example.mirimilibe.auth.jwt.dto;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtMemberDetail extends User {
	private final Long memberId;
	private final String phoneNumber;

	@Builder(builderMethodName = "jwtMemberDetailBuilder")
	public JwtMemberDetail(Long memberId, String phoneNumber, String username, String password,
		Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.memberId = memberId;
		this.phoneNumber = phoneNumber;
	}

}
