package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.RoomUserResponse;

@Service
public class MessegingSenderService {
	public void sendPayload(RoomUserResponse payload) {
		System.out.println("messign sending   "+"/topic/room/"+payload.getRoomId()+"/"+payload.getSender());
		//messagingTemplate.convertAndSend("/topic/room/"+payload.getRoomId()+"/"+payload.getSender(),payload);
	}

	

	
	
	
	
	
	
	
	

}
