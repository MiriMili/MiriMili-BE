package org.example.mirimilibe.global.auth.service;

import java.util.List;

import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;
	private static final String ROLE= "ROLE_USER";

	@Override
	public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
		Member member = memberRepository.findByNumber(phoneNumber)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		return JwtMemberDetail.jwtMemberDetailBuilder()
			.memberId(member.getId())
			.username(member.getId().toString())
			.password(member.getPassword())
			.authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
			.build();

	}
}
