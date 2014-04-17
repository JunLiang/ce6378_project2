package edu.utdallas.ce6378.project2;

public enum MessageType {
	
	/*Request Types*/
	CLIENT_PUT_OBJECT,

	CLIENT_GET_OBJECT,
	
	CLIENT_LIST_OBJECTS, //get all objects
	
	SERVER_PUT_OBJECT,
	
	SERVER_GET_OBJECTS,
	
	/*Server control Types*/
	SERVER_CONTROL_FAIL, //ask server to simulate failure
	
	SERVER_CONTROL_ISOLATE, //ask server to simulate broken link with other servers.
	
	SERVER_CONTROL_RESUME, //Resume all functions
	
	
	
	/*Response Types*/
	SERVER_TO_CLIENT_PUT_OK,

	SERVER_TO_CLIENT_PUT_FAIL,
	
	SERVER_TO_CLIENT_READ_OK,
	
	SERVER_TO_SERVER_PUT_OK,
	
	SERVER_TO_SERVER_PUT_FAIL,
	
	/*Only respond when server simulates failure*/
	SERVER_UNAVAILABLE

}
