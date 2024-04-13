package com.example.demo.utils;

import java.util.HashMap;
import java.util.Map;

public class HandleRoomConnections {
	
	public Map<String, RoomPeerConnection> connections=new HashMap<>();
	private static HandleRoomConnections handleRoomConnections;
	
	
	
	private HandleRoomConnections() {
		
		
	}
	
	public static HandleRoomConnections getHandleRoomConnection() {
		if(handleRoomConnections==null) {
			handleRoomConnections=new HandleRoomConnections();
		}
		return handleRoomConnections;
	}
	
	
	public Map<String, RoomPeerConnection> getTheConnections(){
		return this.connections;
	}

	public Boolean put(String name,RoomPeerConnection room_peer_connection) {
		
		if(!connections.containsKey(name)) {
			this.connections.put(name, room_peer_connection);
			return true;
		}
		return false;
	}
	
	public RoomPeerConnection getPeerConnectionByName(String name) {
		if(this.connections.containsKey(name)) {
			return this.getTheConnections().get(name);
		}
		return null;
	}
	
	public Boolean removeConnection(String name) {
		if(this.connections.containsKey(name)) {
			this.connections.remove(name);
			return true;
		}
		return false;
		
		
	}
}
