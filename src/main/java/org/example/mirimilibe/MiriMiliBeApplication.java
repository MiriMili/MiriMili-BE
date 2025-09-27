package org.example.mirimilibe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiriMiliBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiriMiliBeApplication.class, args);
	}

}
