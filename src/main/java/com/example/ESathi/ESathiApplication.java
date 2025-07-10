package com.example.ESathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ESathiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ESathiApplication.class, args);
	}

}
