package com.example.demo.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.RoomUserResponse;
import com.example.demo.service.RoomJoinService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class RoomController {
	@Autowired
	private RoomJoinService roomJoinService;
	
	@Autowired
	 private SimpMessagingTemplate messagingTemplate;
	 
	 	
	

	
	@MessageMapping("/room")
	public void gertRoomMessaging(@Payload  RoomUserResponse response) {
		 
			 try {
				roomJoinService.handleRoomPayload(response,messagingTemplate);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}        
    
				
	}
	
	@GetMapping("/hello")
	public String getGreeting() {
		String payload=String.format("{\"message\":\"hello\"}", null);
		System.out.println(new RoomJoinService().hashCode());
		return payload;
		
	}

}
