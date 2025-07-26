package org.example.mirimilibe.global.auth.controller;

import org.example.mirimilibe.global.auth.dto.SmsReq;
import org.example.mirimilibe.global.auth.service.CoolSmsService;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<?> SendSMS(@RequestBody @Valid SmsReq req){
		smsService.sendSms(req);
		return ResponseEntity.ok("문자를 전송했습니다.");
	}
}
