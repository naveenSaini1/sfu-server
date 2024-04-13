package com.example.demo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.RoomUserResponse;
import com.example.demo.utils.RoomPeerConnection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCRtpReceiver;
import dev.onvoid.webrtc.RTCRtpSendParameters;
import dev.onvoid.webrtc.RTCRtpSender;
import dev.onvoid.webrtc.RTCRtpTransceiver;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioOptions;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.audio.AudioTrackSource;
import dev.onvoid.webrtc.media.video.VideoDevice;

@Service
public class RoomJoinService {
	
	public static Map<String,Map<String,RTCPeerConnection>> list_of_connected_user=new HashMap<>();
	private static PeerConnectionFactory peerConnectionFactory=new PeerConnectionFactory();
	private static SimpMessagingTemplate simpMessagingTemplate;
	private static Map<String,MediaStreamTrack> allTheTracks=new HashMap<>();
	
	public synchronized RTCPeerConnection  getTehConnections(RoomUserResponse roomUserResponse) {
		System.out.println("the size of map "+list_of_connected_user.size()+" and the inside map"+list_of_connected_user.get(roomUserResponse.getRoomId())+" and the type"+roomUserResponse.getType()+"all track"+allTheTracks.size());;
		RTCPeerConnection peer_connection=null;
		
		if(list_of_connected_user.containsKey(roomUserResponse.getRoomId())) {
			Map<String,RTCPeerConnection> existList= list_of_connected_user.get(roomUserResponse.getRoomId());
			if(existList.containsKey(roomUserResponse.getSender())) {
				peer_connection=existList.get(roomUserResponse.getSender());
				
			}
			else {
				Map<String,RTCPeerConnection> object_forNewlyUser=list_of_connected_user.get(roomUserResponse.getRoomId());
				peer_connection=checkIfConnection(null,roomUserResponse);
				object_forNewlyUser.put(roomUserResponse.getSender(), peer_connection);
				list_of_connected_user.put(roomUserResponse.getRoomId(), object_forNewlyUser);

			}
		}
		else {
			
			Map<String,RTCPeerConnection> object_forNewlyUser=new HashMap<>();
			peer_connection=checkIfConnection(null,roomUserResponse);
			object_forNewlyUser.put(roomUserResponse.getSender(), peer_connection);
			list_of_connected_user.put(roomUserResponse.getRoomId(), object_forNewlyUser);
		}
		return peer_connection;
		
	}
	
	@SuppressWarnings("unused")
	public synchronized void handleRoomPayload(RoomUserResponse roomUserResponse,SimpMessagingTemplate messagin) throws JsonMappingException, JsonProcessingException {
		if(simpMessagingTemplate==null)simpMessagingTemplate=messagin;
		
		RoomPeerConnection peerConnection=new RoomPeerConnection(roomUserResponse.getSender(),messagin);
		RTCPeerConnection peer_connection=getTehConnections(roomUserResponse);
		
		switch (roomUserResponse.getType()) {
		case "offer": {
			
				//System.out.println("offers "+roomUserResponse.getData());
                peerConnection.createAnswer(roomUserResponse,peer_connection);
             

			break;
		}
		case "answer":{
			System.out.println("answer");
			
			break;
		}
		case "icecandidate":{			
		   peerConnection.addIceCandidateMethod(roomUserResponse,peer_connection);
			break;
		}
		default:
			break;
		}
		
	}
	
	public  RTCPeerConnection checkIfConnection(String name, RoomUserResponse roomUserResponse) {
		
		System.out.println("checkIfConnection and purpose"+roomUserResponse.getType());
		
		
		Recoder recoder=new Recoder();
		RTCConfiguration rtcConfiguration = new RTCConfiguration();

		RTCIceServer stunServer = new RTCIceServer();
		stunServer.urls.add("stun:stun.l.google.com:19302");
		rtcConfiguration.iceServers.add(stunServer);
		 RTCPeerConnection peer_connection = peerConnectionFactory.createPeerConnection(rtcConfiguration, new PeerConnectionObserver() {
			
				@Override
				public void onIceCandidate(RTCIceCandidate iceCandidate) {
					if (iceCandidate == null) return;
					
					final String candidate = String.format(
							"{\"sdpMid\":\"%s\", \"sdpMLineIndex\":%d, \"candidate\":\"%s\"}",
							iceCandidate.sdpMid,
							iceCandidate.sdpMLineIndex,
							iceCandidate.sdp
						);
					
					roomUserResponse.setData(candidate);
					roomUserResponse.setType("icecandidate");
					simpMessagingTemplate.convertAndSend("/topic/room/"+roomUserResponse.getRoomId()+"/"+roomUserResponse.getSender(),roomUserResponse);					
				}
	
				@Override
				public void onTrack(RTCRtpTransceiver transceiver) {
					System.err.println("onTrack Method Calls ");
				}
	
				@Override
				public void onConnectionChange(RTCPeerConnectionState state) {
					System.err.println("onConnectionChane Method Calls "+state);
					if(state.equals(RTCPeerConnectionState.CONNECTED)) {
						System.out.println("connected==========>");
						//RoomJoinService.addTrackToThePeer(roomUserResponse);
						//recoder.getTheTracks(allTheTracks.get(0),roomUserResponse);
					}
					if(state.equals(RTCPeerConnectionState.DISCONNECTED)) {
						System.out.println("disconnected============>");
						//recoder.stopRecording(allTheTracks.get(0), roomUserResponse);
					}
				}
	
				@Override
				public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
					final MediaStreamTrack track = receiver.getTrack();
					allTheTracks.put(roomUserResponse.getSender(),track);
					System.err.println("onAddTrack "+ track.getKind());
				}
				
			});
		 
		// AudioTrackSource audioSource = peerConnectionFactory.createAudioSource(new AudioOptions());
		 if(allTheTracks.size()>0) {
			 synchronized(allTheTracks) { // Synchronize access to allTheTracks
			        System.out.println("innnnnnnnnnnnnnnnnnn");
			        AudioTrack audioTrack = (AudioTrack) allTheTracks.get("a");
			        audioTrack.addSink(new AudioTrackSink() {
			            @Override
			            public void onData(byte[] data, int bitsPerSample, int sampleRate, int channels, int frames) {
			                System.out.println(data);
			            }
			        });

			        List<String> streamIds = new ArrayList<>();
			        streamIds.add(allTheTracks.get("a").getId());

			        synchronized(peer_connection) { // Synchronize access to peer_connection
			            RTCRtpSender audioSender = peer_connection.addTrack(audioTrack, streamIds);
			        }
			    }

		 }
		 
		 
		 
//		 int count=allTheTracks.size();
//			System.out.println("size of allthetracks "+count+" and purpose"+roomUserResponse.getType());
//		if(count>0) {
//			
//			 for(MediaStreamTrack track:allTheTracks) {
//				 System.err.println("adding track to the peer connection "+track.getId());
//				 List<String> streamsId=new ArrayList<>();
//				 streamsId.add(track.getId());					
//				peer_connection.addTrack(track, streamsId);
//				
//				 
//			 }
//		}
		 
		 return peer_connection;
		
	}
	

	
	
	
	public static void addTrackToThePeer(RoomUserResponse roomUserResponse) {
		System.out.println("addtrack method ======>");

		Map<String, RTCPeerConnection> savedMapOfConnection = list_of_connected_user.get(roomUserResponse.getRoomId());
	 	if(savedMapOfConnection.size()==1)return;
	 	
			savedMapOfConnection.forEach((key, connection) -> {
				System.err.println(key);
				if(key.equals(roomUserResponse.getSender())) {
					savedMapOfConnection.forEach((inner_key, inner_connection) -> {
						if(!inner_key.equals(key)) {
							System.out.println("connection is "+inner_key+" adding to "+key);
							final int receivers = inner_connection.getReceivers().length;
							System.out.println("receviers lenght ======>"+receivers);
						
							if (receivers == 0) return;
							try {
								for (int index = 0; index < receivers; index++) {
									
									final RTCRtpReceiver receiver = inner_connection.getReceivers()[index];
									if (receiver != null) {
										
										final MediaStreamTrack track = receiver.getTrack();
										if (track != null) {
													    					
											List<String> streamIds = new ArrayList<String>();
											streamIds.add(receiver.getTrack().getId());
											
											@SuppressWarnings("unused")
											RTCRtpSender sender= connection.addTrack(track, streamIds);
										}
									}
								}
								
							}
							catch(Exception e) {
								System.out.println("someting went wrong "+e.getMessage());
								
							}
						}
					});
				}
				else {
					//addTrackToConnection(savedMapOfConnection.get(key),connection);
				}
				
//			final int receivers = connection.getReceivers().length;
//			System.out.println("receviers lenght ======>"+receivers);
//		
//			if (receivers == 0) return;
//			try {
//				for (int index = 0; index < receivers; index++) {
//					
//					final RTCRtpReceiver receiver = connection.getReceivers()[index];
//					if (receiver != null) {
//						
//						final MediaStreamTrack track = receiver.getTrack();
//						if (track != null) {
//									    					
//							List<String> streamIds = new ArrayList<String>();
//							streamIds.add(receiver.getTrack().getId());
//							
//							@SuppressWarnings("unused")
//							RTCRtpSender sender= connection.addTrack(track, streamIds);
//						}
//					}
//				}
//				
//			}
//			catch(Exception e) {
//				System.out.println("someting went wrong "+e.getMessage());
//				
//			}
//			
		  
		});

		
		

		}
	
	public static void addTrackToConnection(RTCPeerConnection fromConnection,RTCPeerConnection toConnection) {
		
		
	}
	
	
//	 for (MediaStreamTrack track : allTheTracks) {
//	        Map<String, RTCPeerConnection> savedMapOfConnection = list_of_connected_user
//	                .get(roomUserResponse.getRoomId());
//	        	savedMapOfConnection.forEach((key, connection) -> {
//	    		try {
//	    			if (track != null) {
//		    							    					
//		    					List<String> streamIds = new ArrayList<String>();
//		    					streamIds.add(track.getId());
//		    					
//		    					@SuppressWarnings("unused")
//		    					RTCRtpSender sender = connection.addTrack(track, streamIds);
//		    				}
//		    			
//	    			
//	    		}
//	    		catch(Exception e) {
//	    			System.out.println("someting went wrong "+e.getMessage());
//	    			
//	    		}
//	    		
//	            
//	        });
//	    }
	
	
		
	
//    Map<String, RTCPeerConnection> savedMapOfConnection = list_of_connected_user
//            .get(roomUserResponse.getRoomId());
//    	savedMapOfConnection.forEach((key, connection) -> {
//    	final int receivers = connection.getReceivers().length;
//		System.out.println("receviers lenght ======>"+receivers);
//
//		if (receivers == 0) return;
//		try {
//			for (int index = 0; index < receivers; index++) {
//    			
//    			final RTCRtpReceiver receiver = connection.getReceivers()[index];
//    			if (receiver != null) {
//    				
//    				final MediaStreamTrack track = receiver.getTrack();
//    				if (track != null) {
//    							    					
//    					List<String> streamIds = new ArrayList<String>();
//    					streamIds.add(receiver.getTrack().getId());
//    					
//    					//@SuppressWarnings("unused")
//    					RTCRtpSender sender= connection.addTrack(track, streamIds);
//    				}
//    			}
//    		}
//			
//		}
//		catch(Exception e) {
//			System.out.println("someting went wrong "+e.getMessage());
//			
//		}
//		
//        
//    });


	


}
