package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.demo.service.MessegingSenderService;

@SpringBootApplication
public class ClinetToServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinetToServerApplication.class, args);
	}
	
}