package edu.utdallas.ce6378.project2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ContentServer {
	
	private Integer nodeId;
	
	//Content stored on each server.
	
	private ConcurrentHashMap<Integer, ContentObject> primaryObjects;
	private ConcurrentHashMap<Integer, ContentObject> secondaryObjects;
	private ConcurrentHashMap<Integer, ContentObject> tertiaryObjects;
	
	//This is used to store the socket of other servers.
	//These sockets should be available at all time
	//When a server simulates failure
	
	private TreeMap<Integer, Socket> serverSockets;
	
	private VectorTimestamp logicTimestamp;
	
	//The current simulation status of the server.
	//It could be normal, or simulate fail or simulate isolation.
	private ServerStatus serverStatus;
	
	private NodeConfiguration config;
	
	/*save the direct connections to other servers to 
	 * initiate server to server communications.
	 */
	private TreeMap<Integer,ObjectOutputStream> outboundPipes;
	private TreeMap<Integer, Socket> outboundSockets;
	
	public ContentServer(NodeConfiguration localNodeConfig) {
		// TODO Auto-generated constructor stub
		config = localNodeConfig;
		logicTimestamp = new VectorTimestamp();		
		primaryObjects = new ConcurrentHashMap<Integer, ContentObject> ();
		secondaryObjects = new ConcurrentHashMap<Integer, ContentObject> ();
		tertiaryObjects = new ConcurrentHashMap<Integer, ContentObject> ();
		serverSockets = new TreeMap<Integer, Socket> ();
		outboundPipes = new TreeMap<Integer,ObjectOutputStream>();
		outboundSockets = new TreeMap<Integer, Socket>();
	}
	
	public ContentServer () {

	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public ConcurrentHashMap<Integer, ContentObject> getPrimaryObjects() {
		return primaryObjects;
	}

	public void setPrimaryObjects(
			ConcurrentHashMap<Integer, ContentObject> primaryObjects) {
		this.primaryObjects = primaryObjects;
	}

	public ConcurrentHashMap<Integer, ContentObject> getSecondaryObjects() {
		return secondaryObjects;
	}

	public void setSecondaryObjects(
			ConcurrentHashMap<Integer, ContentObject> secondaryObjects) {
		this.secondaryObjects = secondaryObjects;
	}

	public ConcurrentHashMap<Integer, ContentObject> getTertiaryObjects() {
		return tertiaryObjects;
	}

	public void setTertiaryObjects(
			ConcurrentHashMap<Integer, ContentObject> tertiaryObjects) {
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
			ObjectOutputStream out  = null;
			try {
				in = new ObjectInputStream(commPort.getInputStream());
				out = new ObjectOutputStream (commPort.getOutputStream());
				
				while (true) {
					try {
						MessageObject oMessage = (MessageObject)in.readObject();
						
						if (oMessage != null && getServerStatus() != ServerStatus.SERVER_FAIL) {
							/*Incoming message, let's adjust my clock*/
							tickMyClock();
							adjustMyClock(oMessage.getTimestamp());
						}
						
						MessageObject returnMessage = handleIncomeMessage(oMessage);
						
						if (returnMessage != null) {
							out.writeObject(returnMessage);
						}					
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
		
	public MessageObject handleIncomeMessage(MessageObject oMessage) {
		MessageType messageType = oMessage.getMessageType();
		ContentObject returnObject = null;
		MessageObject returnMessage = new MessageObject();
		switch (messageType) { 
			case CLIENT_GET_OBJECT :returnObject = handleClientGetRequest(oMessage.getContentObject()); break;
			case CLIENT_PUT_OBJECT :handleClientPutRequest(oMessage.getContentObject()); break;
			case SERVER_PUT_OBJECT :handleServerPutRequest(); break;
			case SERVER_GET_OBJECTS :handleServerGetRequest(); break;
			case SERVER_CONTROL_FAIL: handleSimulationFail(); break;
			default : break;
		}
		returnMessage.setContentObject(returnObject);
		return returnMessage;
	}

	private void handleSimulationFail() {
		// TODO Auto-generated method stub
		
	}

	private void handleServerGetRequest() {
		// TODO Auto-generated method stub
		
	}

	private void handleServerPutRequest() {
		// TODO Auto-generated method stub
		
	}

	private boolean handleClientPutRequest(ContentObject objectParam) {
		// TODO Auto-generated method stub
		boolean putResult = false;
		if (objectParam != null) {
			Integer key = objectParam.getObjId();
			
			Integer primaryHashValue = (key % 7 );
			Integer secondHashvalue = (primaryHashValue + 1) % 7;
			Integer tertiaryHashvalue = (secondHashvalue + 1) % 7;
			
			MessageObject message = new MessageObject();
			
			message.setContentObject(objectParam);
			message.setFromServerId(nodeId);
			message.setMessageType(MessageType.SERVER_PUT_OBJECT);
			
			//TODO, need a non blocking way to send the write request 
			//to other severs and wait for an answer.
			
			if (primaryHashValue != this.nodeId) {
				try {
					this.outboundPipes.get(primaryHashValue).writeObject(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (secondHashvalue != this.nodeId) {
				try {
					this.outboundPipes.get(secondHashvalue).writeObject(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (tertiaryHashvalue != this.nodeId) {
				try {
					this.outboundPipes.get(tertiaryHashvalue).writeObject(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
		
		return putResult;
		
	}

	private ContentObject handleClientGetRequest(ContentObject objectParam) {
		// TODO Auto-generated method stub
		
		ContentObject returnObject = null;
		if (objectParam != null) {
			Integer key = objectParam.getObjId();
			
			Integer primaryHashValue = (key % 7 );
			Integer secondHashvalue = (primaryHashValue + 1) % 7;
			Integer tertiaryHashvalue = (secondHashvalue + 1) % 7;
			
			if (primaryHashValue.equals(nodeId)) {
				returnObject = this.primaryObjects.get(key); 
			} else if (secondHashvalue.equals(nodeId)) {
				returnObject = this.secondaryObjects.get(key); 
			} else if (tertiaryHashvalue.equals(key)){
				returnObject = this.tertiaryObjects.get(key);
			}
		}
		
		return returnObject;
		
	}
	
	private void establishConnectionToServers() throws UnknownHostException, IOException {
		/*save all the connections to the other servers */
		for (NodeConfiguration aConfig : Main.getAllNodes()) {
			if (aConfig.getNodeId() != this.config.getNodeId()) {
				Socket socket = new Socket(aConfig.getHostName(), aConfig.getPortNo());
				this.outboundSockets.put(aConfig.getNodeId(), socket);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				this.outboundPipes.put(aConfig.getNodeId(), out);
			}
		}
	}


	public void start() throws UnknownHostException, IOException {
		// Start Listener thread
		ListenerThread listener = new ListenerThread(config.getPortNo());
		
		Thread listenerThread = new Thread(listener);
		
		listenerThread.start();

		/*now wait for other servers to come up before 
		 * creating connections to other servers */
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		
		establishConnectionToServers();	
	}

}
