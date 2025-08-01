package com.steam.modeni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModeniApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModeniApplication.class, args);
	}

}
