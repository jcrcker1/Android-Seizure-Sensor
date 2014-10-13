package com.seizuresensor;

public class Packet {
	
	int _id;
	String command;
	String time;
	int size;
	int payload;
	String _gps_data;
	String _user_response;
	String raw;
	
	public Packet(){
	}
	
	public Packet(int id, String Command, String Time, int Size, int Payload, String gps_data, String user_response, String Raw){
		this._id = id;
		this.command = Command;
		this.time = Time;
		this.size = Size;
		this.payload = Payload;
		this.raw = Raw;
		this._gps_data = gps_data;
		this._user_response = user_response;
	}
	
	public Packet(String Command, String Time, int Size, int Payload, String gps_data, String user_response){
		this.command = Command;
		this.time = Time;
		this.size = Size;
		this.payload = Payload;
		this._gps_data = gps_data;
		this._user_response = user_response;
	}

	public Packet(String packetString){
		char[] packetArray = packetString.toCharArray();
		this.command = "" + packetArray[0];
		this.time = "";
		String aaa = "";
		for(int i=1; i<15; i++){
			this.time = this.time + "" + packetArray[i];
		}
		
		this.size = Integer.parseInt("" + packetArray[15]);
		
		for(int i=0; i<this.size; i++){
			aaa = aaa + packetArray[16+i];
		}
		
		this.payload = Integer.parseInt(aaa);
		this.raw = packetString;
	}
	
	public char[] topacketArray(){
		char[] packetArray = this.raw.toCharArray();
		return packetArray;
	}
	
	// getting ID
	public int getID(){
		return this._id;
	}
	
	// setting id
	public void setID(int id){
		this._id = id;
	}
	
	// getting time
	public String getTime(){
		return this.time;
	}
	
	// setting time
	public void setTime(String time){
		this.time = time;
	}
	
	// getting data
	public int getData(){
		return this.payload;
	}
	
	// setting data
	public void setData(int data){
		this.payload = data;
	}
	
	// getting gps data
	public String getCommand(){
		return this.command;
	}
	
	public void setCommand(String command){
		this.command = command;
	}
	
	public int getSize(){
		return this.size;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	// getting gps data
	public String getGPSData(){
		return this._gps_data;
	}
	
	// setting gps data
	public void setGPSData(String _gps_data){
		this._gps_data = _gps_data;
	}
	
	// getting user response
	public String getUserResponse(){
		return this._user_response;
	}
	
	// setting user response
	public void setUserResponse(String userResponse){
		this._user_response = userResponse;
	}
	
}
