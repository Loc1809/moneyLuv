package org.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoneyLuvApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyLuvApplication.class, args);
	}

}
