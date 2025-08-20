package org.example.mirimilibe.global.auth.service;

import java.time.Duration;

import org.example.mirimilibe.global.auth.dto.SmsReq;
import org.example.mirimilibe.global.auth.dto.SmsVerifyReq;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.SmsErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoolSmsService {
	@Value("${cool-sms.api.key}")
	private String apiKey;

	@Value("${cool-sms.api.secret}")
	private String apiSecret;

	@Value("${cool-sms.api.number}")
	private String fromPhoneNumber;

	private DefaultMessageService messageService;
	private final StringRedisTemplate stringRedisTemplate;
	private final MemberRepository memberRepository;

	private final int LIMIT_TIME = 60 * 3;

	public CoolSmsService(StringRedisTemplate stringRedisTemplate, MemberRepository memberRepository) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.memberRepository = memberRepository;
	}

	@PostConstruct
	private void init() {
		this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
	}

	private void sendSms(SmsReq smsReq) {
		String certificationCode = generateCertificationCode();
		String str = String.format("[미리밀리] 인증번호는 %s 입니다.", certificationCode);

		Message message = new Message();
		message.setFrom(fromPhoneNumber);
		message.setTo(smsReq.phoneNumber());
		message.setText(str);

		try {
			// Redis에 인증 코드 저장
			stringRedisTemplate.opsForValue().set("sms:"+smsReq.phoneNumber(), certificationCode, Duration.ofSeconds(LIMIT_TIME));
			messageService.send(message);

			log.info("[sms] 인증번호 전송 성공, 전화번호: {}", smsReq.phoneNumber());
		} catch (Exception e) {
			log.error("[sms] 인증번호 전송 실패, 전화번호: {}, 원인: {}", smsReq.phoneNumber(), e.getMessage());
			throw new MiriMiliException(SmsErrorCode.SEND_SMS_FAILED);
		}
	}

	public void verifySms(SmsVerifyReq req) {
		String storedCode = stringRedisTemplate.opsForValue().get("sms:" + req.phoneNumber());
		if (storedCode == null || !storedCode.equals(req.certificationCode())) {
			throw new MiriMiliException(SmsErrorCode.VERIFICATION_FAILED);
		}
		// 인증 성공 후 Redis에서 인증 코드 삭제
		stringRedisTemplate.delete("sms:" + req.phoneNumber());
		log.info("[sms] 인증번호 검증 성공, 전화번호: {}", req.phoneNumber());
	}

	private String generateCertificationCode() {
		return Integer.toString((int) (Math.random() * (999999 - 100000 + 1)) + 100000);
	}

	public void sendSignUpSms(SmsReq smsReq) {
		// 회원가입 시 문자 인증번호 발송
		if (memberRepository.existsByNumber(smsReq.phoneNumber())) {
			throw new MiriMiliException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
		}
		sendSms(smsReq);
	}

}
