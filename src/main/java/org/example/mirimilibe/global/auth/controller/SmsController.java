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

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {
	private final CoolSmsService smsService;


	@PostMapping("/send")
	@Operation(summary = "문자 인증번호 발송",
		description = "휴대폰 번호로 문자 인증번호를 발송합니다. "
		+ "발급된 인증번호는 3분 동안 유효하며 숫자 6자리로 구성됩니다. "
		+ "발송된 인증번호는 레디스에 저장되며, 이후 검증을 위해 사용됩니다. ")
	public ResponseEntity<ApiResponse<String>> SendSMS(@RequestBody @Valid SmsReq req){
		smsService.sendSignUpSms(req);
		return ResponseEntity.ok(ApiResponse.success("문자 인증번호가 발송되었습니다."));
	}

	@GetMapping("/send-pwd")
	@Operation(summary = "비밀번호 변경 문자 인증번호 발송",
		description = "비밀번호 변경을 위한 문자 인증번호를 발송합니다. "
		+ "발급된 인증번호는 3분 동안 유효하며 숫자 6자리로 구성됩니다. "
		+ "발송된 인증번호는 레디스에 저장되며, 이후 검증을 위해 사용됩니다.")
	public ResponseEntity<ApiResponse<String>> SendPwdSMS(@RequestBody @Valid SmsReq req){
		smsService.sendPwdChangeSms(req);
		return ResponseEntity.ok(ApiResponse.success("비밀번호 변경을 위한 문자 인증번호가 발송되었습니다."));
	}

	@PostMapping("/verify")
	@Operation(summary = "문자 인증번호 검증",
		description = "발송된 문자 인증번호를 검증합니다. <br>"
		+ "회원가입 또는 비밀번호 재설정 전에 이 API를 호출하여 전화번호를 검증해야 합니다."
		+ "인증된 상태는 5분 동안 유효합니다. <br>")
	public ResponseEntity<ApiResponse<String>> VerifySMS(@RequestBody @Valid SmsVerifyReq req) {
		smsService.verifySms(req);
		return ResponseEntity.ok(ApiResponse.success("문자 인증이 완료되었습니다."));
	}
}
