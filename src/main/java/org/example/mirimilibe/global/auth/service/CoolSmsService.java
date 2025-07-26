package org.example.mirimilibe.global.auth.service;

import java.time.Duration;

import org.example.mirimilibe.global.auth.dto.SmsReq;
import org.example.mirimilibe.global.auth.dto.SmsVerifyReq;
import org.example.mirimilibe.global.error.SmsErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Service
public class CoolSmsService {
	@Value("${cool-sms.api.key}")
	private String apiKey;

	@Value("${cool-sms.api.secret}")
	private String apiSecret;

	@Value("${cool-sms.api.number}")
	private String fromPhoneNumber;

	//레디스
	private final StringRedisTemplate stringRedisTemplate;

	private final int LIMIT_TIME = 60 * 3; // 5분

	public CoolSmsService(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	public void sendSms(SmsReq smsReq) {
		DefaultMessageService messageService =  NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");

		String certificationCode = generateCertificationCode();
		String str = String.format("인증번호는 %s 입니다.", certificationCode);

		Message message = new Message();
		message.setFrom(fromPhoneNumber);
		message.setTo(smsReq.phoneNumber());
		message.setText(str);

		try {
			// Redis에 인증 코드 저장
			stringRedisTemplate.opsForValue().set("sms:"+smsReq.phoneNumber(), certificationCode, Duration.ofSeconds(LIMIT_TIME));
			messageService.send(message);
		} catch (Exception e) {
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

	}

	private String generateCertificationCode() {
		return Integer.toString((int) (Math.random() * (999999 - 100000 + 1)) + 100000);
	}

}
