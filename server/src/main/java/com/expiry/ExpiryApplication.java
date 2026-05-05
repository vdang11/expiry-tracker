package com.expiry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpiryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpiryApplication.class, args);
	}
}