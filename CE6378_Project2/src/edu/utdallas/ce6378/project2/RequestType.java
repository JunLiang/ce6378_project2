package edu.utdallas.ce6378.project2;

public enum RequestType {
	
	CLIENT_PUT_OBJECT,

	CLIENT_GET_OBJECT,
	
	CLIENT_LIST_OBJECTS, //get all objects
	
	SERVER_PUT_OBJECT,
	
	SERVER_GET_OBJECTS,
	
	SERVER_CONTROL_FAIL, //ask server to simulate failure
	
	SERVER_CONTROL_ISOLATE, //ask server to simulate broken link with other servers.
	
	SERVER_CONTROL_RESUME //Resume all functions

}
