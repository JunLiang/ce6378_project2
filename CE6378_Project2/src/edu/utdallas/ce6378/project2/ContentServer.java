package edu.utdallas.ce6378.project2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ContentServer {
	
	private Integer nodeId;
	
	//Content stored on each server.
	
	private ConcurrentHashMap<Long, ContentObject> primaryObjects;
	private ConcurrentHashMap<Long, ContentObject> secondaryObjects;
	private ConcurrentHashMap<Long, ContentObject> tertiaryObjects;
	
	
	
	class ListenerThread implements Runnable {
		
		private ServerSocket listenerPort;
		
		public ListenerThread (ServerSocket socket) {
			listenerPort = socket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				try {
					Socket socket = listenerPort.accept();
					IncomingMessageHandlingThread msgThread = new IncomingMessageHandlingThread(socket);
					Thread thread = new Thread(msgThread);
					thread.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	class IncomingMessageHandlingThread implements Runnable {
		
		private Socket commPort;
		
		public IncomingMessageHandlingThread(Socket socket) {
			commPort = socket; 
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ObjectInputStream in  = null;
			try {
				in = new ObjectInputStream(commPort.getInputStream());
				while (true) {
					try {
						ObjectRequest oRequest = (ObjectRequest)in.readObject();
						handleIncomeMessage(oRequest);
						
					}catch (EOFException eofex) {
						eofex.printStackTrace(System.err);
					}catch (IOException ioex) {
						ioex.printStackTrace(System.err);
					}catch (ClassNotFoundException cnfex) {
						// TODO Auto-generated catch block
						cnfex.printStackTrace(System.err);
					}
				}
			} catch (IOException ioex) {
				ioex.printStackTrace(System.err);
			}  finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	public void handleIncomeMessage(ObjectRequest oRequest) {
		RequestType requestType = oRequest.getRequestType();
		switch (requestType) 
		case :
		if (oRequest.getRequestType() == RequestType.CLIENT_GET_OBJECT) {
			handleClientGetRequest();
		} else if ()
	}

	private void handleClientGetRequest() {
		// TODO Auto-generated method stub
		
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public ConcurrentHashMap<Long, ContentObject> getPrimaryObjects() {
		return primaryObjects;
	}

	public void setPrimaryObjects(
			ConcurrentHashMap<Long, ContentObject> primaryObjects) {
		this.primaryObjects = primaryObjects;
	}

	public ConcurrentHashMap<Long, ContentObject> getSecondaryObjects() {
		return secondaryObjects;
	}

	public void setSecondaryObjects(
			ConcurrentHashMap<Long, ContentObject> secondaryObjects) {
		this.secondaryObjects = secondaryObjects;
	}

	public ConcurrentHashMap<Long, ContentObject> getTertiaryObjects() {
		return tertiaryObjects;
	}

	public void setTertiaryObjects(
			ConcurrentHashMap<Long, ContentObject> tertiaryObjects) {
		this.tertiaryObjects = tertiaryObjects;
	}
		

}
