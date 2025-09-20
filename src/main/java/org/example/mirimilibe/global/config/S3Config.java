package org.example.mirimilibe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {
	@Value("${app.s3.region}") String region;

	@Bean(destroyMethod = "close")
	S3Presigner s3Presigner() {
		return S3Presigner.builder().region(Region.of(region)).build();
	}

	@Bean(destroyMethod = "close")
	S3Client s3() {
		return S3Client.builder().region(Region.of(region)).build();
	}
}