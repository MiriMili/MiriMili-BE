package org.example.mirimilibe.global.auth.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.SmsReq;
import org.example.mirimilibe.global.auth.dto.SmsVerifyReq;
import org.example.mirimilibe.global.auth.service.CoolSmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {
	private final CoolSmsService smsService;


	@PostMapping("/send")
	public ApiResponse<?> SendSMS(@RequestBody @Valid SmsReq req){
		smsService.sendSms(req);
		return ApiResponse.success("문자 인증 요청이 성공적으로 전송되었습니다.");
	}

	@GetMapping("/verify")
	public ApiResponse<?> VerifySMS(@RequestBody @Valid SmsVerifyReq req) {
		smsService.verifySms(req);
		return ApiResponse.success("문자 인증이 성공적으로 완료되었습니다.");
	}
}
