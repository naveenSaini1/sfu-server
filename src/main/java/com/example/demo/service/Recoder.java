package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.model.RoomUserResponse;

import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;

public class Recoder implements AudioTrackSink {
	public RoomUserResponse roomUserResponse=null;
	private static Map<String, ByteArrayOutputStream> voice_data=new HashMap<>();


	public void startRecoding(MediaStreamTrack track, RoomUserResponse response) {
		
		System.out.println("stating reocding method " + track.getId());
		roomUserResponse=response;
		AudioTrackSink sink = this;
		((AudioTrack) track).addSink(sink);

	}

	public void stopRecording(MediaStreamTrack track, RoomUserResponse response)  {
	
		try {
			roomUserResponse=response;
			System.err.println("stoopiong  " + track.getId());
			AudioTrackSink sink = this;
			((AudioTrack) track).removeSink(sink);
			ByteArrayOutputStream stream=voice_data.remove(response.getSender());
			System.out.println("size of file list  "+stream.toByteArray().length);
			saveFile(stream.toByteArray(),response.getSender());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void getTheTracks(MediaStreamTrack track, RoomUserResponse response) {
		System.out.println("stared recoding");
		List<String> streamIds = new ArrayList<>();
		streamIds.add("stream-0");
		startRecoding(track, response);
	}

	@Override
	public void onData(byte[] data, int bitsPerSample, int sampleRate, int channels, int frames) {
		// TODO Auto-generated method stub
		System.out.println("hello");
      
		try {
		ByteArrayOutputStream bytes	=null;
		if(voice_data.containsKey(roomUserResponse.getSender())) {
				bytes	= voice_data.get(roomUserResponse.getSender());
				bytes.write(data);
				voice_data.put(roomUserResponse.getSender(), bytes);
			} 
		else {
				bytes=new ByteArrayOutputStream();
				bytes.write(data);
				voice_data.put(roomUserResponse.getSender(), bytes);
			
		}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void saveFile(byte[] bytes,String name) throws IOException {
		 String uploadDir = "/home/naveen/Downloads/downloads/hey/";

       // Create the directory if it doesn't exist
       File directory = new File(uploadDir);
       if (!directory.exists()) {
           directory.mkdirs();
       }

       // Get the file bytes and save it to disk
       String filePath = uploadDir + name+".mp4";
       Path path = Paths.get(filePath);
       Files.write(path, bytes);
		
	}

}
