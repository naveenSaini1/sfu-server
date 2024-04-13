package com.example.demo.utils;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.demo.model.RoomUserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCAnswerOptions;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCRtpReceiver;
import dev.onvoid.webrtc.RTCRtpTransceiver;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import dev.onvoid.webrtc.media.MediaStream;

public class RoomPeerConnection{
	
	//public RTCPeerConnection peer_connection;
	
	
	private SimpMessagingTemplate messagingTemplate;
	 
	public String sender_name;
	
	public RoomPeerConnection() {
		
	}
	
	public RoomPeerConnection(String name,SimpMessagingTemplate messagingTemplate) {
		this.sender_name=name;
		this.messagingTemplate=messagingTemplate;
	}

	public void createAnswer(RoomUserResponse roomUserResponse,RTCPeerConnection peer_connection) {
		final String sdpType = "offer";
		final RTCSdpType remoteSdpType = (sdpType.equalsIgnoreCase("offer")) ? RTCSdpType.OFFER : RTCSdpType.ANSWER;
		final RTCSessionDescription remoteDescription = new RTCSessionDescription(remoteSdpType, (String)roomUserResponse.getData());

		if (remoteSdpType == RTCSdpType.OFFER) {
			System.out.println("sdp is an offer");
		} else {
			System.out.println("sdp is an answer");
		}
		
		final SetSessionDescriptionObserver setLocalDescriptionObserver =
			new SetSessionDescriptionObserver() {

				@Override
				public void onSuccess() {
					System.out.println("Local Description Set");
					
					
				}

				@Override
				public void onFailure(String error) {
					System.out.println("Error setting local description");
					
				} 
			};

		final CreateSessionDescriptionObserver createSessionDescriptionObserver = 
			new CreateSessionDescriptionObserver() {

				@Override
				public void onSuccess(RTCSessionDescription description) {
					
					peer_connection.setLocalDescription(description, setLocalDescriptionObserver);
					

					roomUserResponse.setData(description.sdp.toString());
					roomUserResponse.setType(description.sdpType.toString().toLowerCase());
					
					messagingTemplate.convertAndSend("/topic/room/"+roomUserResponse.getRoomId()+"/"+roomUserResponse.getSender(),roomUserResponse);
//					
					System.out.println("Answer created");
					
				}

				@Override
				public void onFailure(String error) {
					System.out.println("Could not create Answer");
					
				}
		
			};		
				
		final SetSessionDescriptionObserver setRemoteDescriptionObserver =
			new SetSessionDescriptionObserver() {

				@Override
				public void onSuccess() {

					System.out.println("Remote Description Set");
					
					if (remoteSdpType == RTCSdpType.OFFER) {
						System.out.println("Creating Answer");
						
						final RTCAnswerOptions rtcAnswerOptions = new RTCAnswerOptions();
						rtcAnswerOptions.voiceActivityDetection = false;
						
						peer_connection.createAnswer(rtcAnswerOptions, createSessionDescriptionObserver);

					} else {
						System.out.println("Remote  set "+ sdpType);

					}
				}

				@Override
				public void onFailure(String error) {
					
					System.out.println("Could not set Remote Description "+ sdpType);
					
				} 
			};
		
			peer_connection.setRemoteDescription(remoteDescription, setRemoteDescriptionObserver);
		
	
		
	}
	
	public void addIceCandidateMethod(RoomUserResponse roomUserResponse,RTCPeerConnection peer_connection) throws JsonMappingException, JsonProcessingException {
		
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(roomUserResponse.getData());
        
        String candidate = (String) jsonNode.get("candidate").asText();
        String sdpMid = (String) jsonNode.get("sdpMid").asText();
        Integer sdpMLineIndex = (Integer) jsonNode.get("sdpMLineIndex").asInt();
        String usernameFragment = (String) jsonNode.get("usernameFragment").asText();
		RTCIceCandidate rtcCandidate = new RTCIceCandidate(sdpMid, sdpMLineIndex, candidate);
        peer_connection.addIceCandidate(rtcCandidate);
        

	}
	
	
}
