package org.example.mirimilibe.global.auth.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.example.mirimilibe.global.auth.dto.PresignPutResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.ulid.UlidCreator;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3UploadService {

	private final S3Presigner presigner;
	private final S3Client s3;

	@Value("${app.s3.bucket}") private String bucket;
	@Value("${app.s3.base-dir}") private String baseDir;
	@Value("${app.s3.put-expiry-minutes}") private long putExpiryMinutes;
	@Value("${app.s3.get-expiry-minutes}") private long getExpiryMinutes;

	private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");
	private static final long MAX_SIZE = 10 * 1024 * 1024L; // 10MB 예시

	public PresignPutResponse presignPut(Long userId, String contentType, long contentLength) {
		if (!ALLOWED.contains(contentType)) throw new IllegalArgumentException("unsupported content-type");
		if (contentLength <= 0 || contentLength > MAX_SIZE) throw new IllegalArgumentException("invalid size");

		String key = buildKey(userId, contentType);

		PutObjectRequest put = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType(contentType)
			.build();

		PutObjectPresignRequest preq = PutObjectPresignRequest.builder()
			.putObjectRequest(put)
			.signatureDuration(Duration.ofMinutes(putExpiryMinutes))
			.build();

		PresignedPutObjectRequest presigned = presigner.presignPutObject(preq);

		return new PresignPutResponse(key, presigned.url().toString());
	}

	public String presignGet(String key) {
		GetObjectRequest get = GetObjectRequest.builder()
			.bucket(bucket).key(key).build();

		GetObjectPresignRequest req = GetObjectPresignRequest.builder()
			.getObjectRequest(get)
			.signatureDuration(Duration.ofMinutes(getExpiryMinutes))
			.build();

		PresignedGetObjectRequest p = presigner.presignGetObject(req);
		return p.url().toString();
	}

	public Map<String, String> presignGets(Collection<String> keys) {
		Map<String, String> map = new LinkedHashMap<>();
		for (String k : keys) map.put(k, presignGet(k));
		return map;
	}

	private String buildKey(Long userId, String contentType) {
		String ext = switch (contentType) {
			case "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> "bin";
		};
		String ym = DateTimeFormatter.ofPattern("yyyy/MM").format(LocalDate.now());
		String ulid = UlidCreator.getUlid().toString();
		return "%s/%s/u%s/%s.%s".formatted(baseDir, ym, userId, ulid, ext);
	}

}
