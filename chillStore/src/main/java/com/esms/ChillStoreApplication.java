package com.esms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class ChillStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChillStoreApplication.class, args);
	}
}
