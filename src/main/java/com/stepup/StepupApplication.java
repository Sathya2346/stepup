package com.stepup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StepupApplication {

	public static void main(String[] args) {
		System.setProperty("spring.classformat.ignore", "true");
		SpringApplication.run(StepupApplication.class, args);
	}

}