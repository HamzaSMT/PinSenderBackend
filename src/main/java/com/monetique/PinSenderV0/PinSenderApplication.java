package com.monetique.PinSenderV0;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PinSenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PinSenderApplication.class, args);
	}
}
