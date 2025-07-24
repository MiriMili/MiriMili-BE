package org.example.mirimilibe.common.config;

import java.util.List;

import org.example.mirimilibe.auth.jwt.filter.JsonAuthenticationFilter;
import org.example.mirimilibe.auth.jwt.filter.JwtAuthenticationFilter;
import org.example.mirimilibe.auth.jwt.handler.LoginFailureHandler;
import org.example.mirimilibe.auth.jwt.handler.LoginSuccessHandler;
import org.example.mirimilibe.auth.jwt.service.LoginService;
import org.example.mirimilibe.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsService loginService;
	private final JwtTokenUtil jwtTokenUtil;
	private final ObjectMapper objectMapper;
	private final MemberRepository memberRepository;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.formLogin(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
			.httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
			.cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/signup").permitAll() // 회원가입 허용
				.requestMatchers("/swagger", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll() // Swagger 허용
				.anyRequest().authenticated() // 나머지 요청은 인증 필요
			);


		httpSecurity
			.addFilterBefore(jsonAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return httpSecurity.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
		configuration.addAllowedHeader("*"); // 모든 헤더 허용
		configuration.setAllowCredentials(true); // 자격 증명 허용
		configuration.addExposedHeader("Authorization"); // 클라이언트가 Authorization 헤더를 읽을 수 있도록 허용

		configuration.setAllowedOrigins(List.of(
			"http://localhost:3000",
			"http://localhost:8080"
		));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용

		return source;
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenUtil);
	}

	@Bean
	public JsonAuthenticationFilter jsonAuthenticationFilter() {
		JsonAuthenticationFilter jsonAuthenticationFilter = new JsonAuthenticationFilter(objectMapper);

		jsonAuthenticationFilter.setAuthenticationManager(authenticationManager());
		jsonAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
		jsonAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler());

		return jsonAuthenticationFilter;
	}

	@Bean
	public LoginSuccessHandler loginSuccessHandler() {
		return new LoginSuccessHandler(memberRepository, jwtTokenUtil, objectMapper);
	}

	@Bean
	public LoginFailureHandler loginFailureHandler() {
		return new LoginFailureHandler(objectMapper);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(){
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(loginService);

		return new ProviderManager(provider);
	}

}
