package org.example.mirimilibe.auth.jwt.handler;

import java.io.IOException;

import org.example.mirimilibe.auth.jwt.dto.LoginSuccessRes;
import org.example.mirimilibe.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final MemberRepository memberRepository;
	private final JwtTokenUtil jwtTokenProvider;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		// 인증이 성공한 후에 호출되는 메서드

		//1. 인증 정보에서 사용자 정보를 가져옵니다.
		UserDetails userDetail = (UserDetails) authentication.getPrincipal();

		log.info("로그인 성공: {}", userDetail.getUsername());

		//2. 사용자 정보를 기반으로 Member 객체를 조회합니다.
		Member member = memberRepository.findById(Long.valueOf(userDetail.getUsername()))
			.orElseThrow(() -> new RuntimeException("no such member"));

		//3. Member 객체를 사용하여 Authentication 객체를 생성합니다.
		Authentication genAuthentication = jwtTokenProvider.createAuthentication(member);

		String accessToken = jwtTokenProvider.generateAccessToken(genAuthentication);

		response.setHeader("Authorization", accessToken);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");

		//4. 응답 본문
		LoginSuccessRes loginSuccessRes=LoginSuccessRes.of(accessToken, member.getNickname());

		//5. 응답을 JSON 형식으로 작성합니다.
		try {
			String responseBody = objectMapper.writeValueAsString(loginSuccessRes);
			response.getWriter().write(responseBody);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
