package edu.utdallas.ce6378.project2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ContentServer {
	
	private Integer nodeId;
	
	//Content stored on each server.
	
	private ConcurrentHashMap<Long, ContentObject> primaryObjects;
	private ConcurrentHashMap<Long, ContentObject> secondaryObjects;
	private ConcurrentHashMap<Long, ContentObject> tertiaryObjects;
	
	//This is used to store the socket of other servers.
	//These sockets should be available at all time
	//When a server simulates failure
	
	private TreeMap<Integer, Socket> serverSockets;
	
	private VectorTimestamp logicTimestamp;
	
	//The current simulation status of the server.
	//It could be normal, or simulate fail or simulate isolation.
	private ServerStatus serverStatus;
	
	private NodeConfiguration config;

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

	public TreeMap<Integer, Socket> getServerSockets() {
		return serverSockets;
	}

	public void setServerSockets(TreeMap<Integer, Socket> serverSockets) {
		this.serverSockets = serverSockets;
	}

	public VectorTimestamp getLogicTimestamp() {
		return logicTimestamp;
	}

	public void setLogicTimestamp(VectorTimestamp logicTimestamp) {
		this.logicTimestamp = logicTimestamp;
	}

	public ServerStatus getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;
	}
	
	public ContentServer () {
		logicTimestamp = new VectorTimestamp();		
		primaryObjects = new ConcurrentHashMap<Long, ContentObject> ();
		secondaryObjects = new ConcurrentHashMap<Long, ContentObject> ();
		tertiaryObjects = new ConcurrentHashMap<Long, ContentObject> ();
		serverSockets = new TreeMap<Integer, Socket> ();
	}
	
	public ContentServer(NodeConfiguration localNodeConfig) {
		// TODO Auto-generated constructor stub
		config = localNodeConfig;
	}

	public synchronized void tickMyClock() {
		logicTimestamp.tickVectorTimestamp(nodeId);
	}
	
	public synchronized void adjustMyClock(VectorTimestamp newTimestamp) {
		//Adjust the local logic clock based on the request time clock
		logicTimestamp.adjustVectorTimestamp(newTimestamp, nodeId);		
	}
	
	class ListenerThread implements Runnable {
		
		private ServerSocket listenerPort;
		
		public ListenerThread (Integer serverPortNo) throws IOException {
			listenerPort = new ServerSocket(serverPortNo);
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
						RequestObject oRequest = (RequestObject)in.readObject();
						
						if (oRequest != null && getServerStatus() != ServerStatus.SERVER_FAIL) {
							/*Incoming message, let's adjust my clock*/
							tickMyClock();
							adjustMyClock(oRequest.getRequestTimestamp());
						}
						
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
	
	public void handleIncomeMessage(RequestObject oRequest) {
		RequestType requestType = oRequest.getRequestType();
		switch (requestType) { 
			case CLIENT_GET_OBJECT :handleClientGetRequest(); break;
			case CLIENT_PUT_OBJECT :handleClientPutRequest(); break;
			case SERVER_PUT_OBJECT :handleServerPutRequest(); break;
			case SERVER_GET_OBJECTS :handleServerGetRequest(); break;
			default : break;
		}
	}

	private void handleServerGetRequest() {
		// TODO Auto-generated method stub
		
	}

	private void handleServerPutRequest() {
		// TODO Auto-generated method stub
		
	}

	private void handleClientPutRequest() {
		// TODO Auto-generated method stub
		
	}

	private void handleClientGetRequest() {
		// TODO Auto-generated method stub
		
	}
	
	private void establishConnectionToServers() {
		for (NodeConfiguration aConfig : Main.getAllNodes()) {
			
		}
	}


	public void start() throws IOException {
		// Start Listener thread
		ListenerThread listener = new ListenerThread(config.getPortNo());
		
		Thread listenerThread = new Thread(listener);
		
		listenerThread.start();

		/*now create connections to other servers */
		
		
	}
		

}
