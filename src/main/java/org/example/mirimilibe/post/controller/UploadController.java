package org.example.mirimilibe.post.controller;


import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.auth.dto.PresignPutRequest;
import org.example.mirimilibe.global.auth.dto.PresignPutResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.example.mirimilibe.global.auth.service.S3UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

	private final S3UploadService s3;

	@PostMapping("/images/presign")
	public PresignPutResponse presign(@Validated @RequestBody PresignPutRequest body,
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		return s3.presignPut(jwtMemberDetail.getMemberId(), body.contentType(), body.contentLength());
	}

	@GetMapping("/images/view-url")
	public Map<String, String> viewUrl(@RequestParam String key) {
		return Map.of("key", key, "url", s3.presignGet(key));
	}

	public record ViewUrlsRequest(List<String> keys) {}
	@PostMapping("/images/view-urls")
	public Map<String, String> viewUrls(@RequestBody ViewUrlsRequest req) {
		return s3.presignGets(req.keys());
	}
}