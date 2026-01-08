package com.nexus.pdsw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PdswApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(PdswApplication.class, args);
	}

}
