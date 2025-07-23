package org.example.mirimilibe.auth.jwt.service;

import java.util.List;

import org.example.mirimilibe.auth.jwt.dto.JwtMemberDetail;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

	private final MemberRepository memberRepository;
	private static final String ROLE= "ROLE_USER";

	@Override
	public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
		Member member = memberRepository.findByNumber(phoneNumber)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

		return JwtMemberDetail.jwtMemberDetailBuilder()
			.memberId(member.getId())
			.phoneNumber(member.getNumber())
			.username(member.getId().toString())
			.password(member.getPassword())
			.authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
			.build();

	}
}
