package org.example.mirimilibe.global.auth.service;

import org.example.mirimilibe.global.auth.dto.SmsReq;
import org.example.mirimilibe.global.error.SmsErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.springframework.beans.factory.annotation.Value;
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

	public void sendSms(SmsReq smsReq) {
		DefaultMessageService messageService =  NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");

		String certificationCode = generateCertificationCode();
		String str = String.format("인증번호는 %s 입니다.", certificationCode);

		Message message = new Message();
		message.setFrom(fromPhoneNumber);
		message.setTo(smsReq.phoneNumber());
		message.setText(str);

		try {
			messageService.send(message);
		} catch (Exception e) {
			throw new MiriMiliException(SmsErrorCode.SEND_SMS_FAILED);
		}

	}

	private String generateCertificationCode() {
		return Integer.toString((int) (Math.random() * (999999 - 100000 + 1)) + 100000);
	}

}
