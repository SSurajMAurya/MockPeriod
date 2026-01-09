package com.mockperiod.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MockPeriodApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockPeriodApplication.class, args);
		
	}

}
