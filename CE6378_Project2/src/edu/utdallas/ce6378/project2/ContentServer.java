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
		MessageObject returnMessage = null;
		switch (messageType) { 
			case CLIENT_GET_OBJECT :returnMessage = handleClientGetRequest(oMessage.getContentObject()); break;
			case CLIENT_PUT_OBJECT :returnMessage = handleClientPutRequest(oMessage.getContentObject()); break;
			case SERVER_PUT_OBJECT :returnMessage = handleServerPutRequest(); break;
			case SERVER_GET_OBJECTS :returnMessage = handleServerGetRequest(); break;
			case SERVER_CONTROL_FAIL:returnMessage = handleSimulationFail(); break;
			default : break;
		}		
		
		return returnMessage;
	}

	private MessageObject handleSimulationFail() {
		// TODO Auto-generated method stub
		return null;
		
	}

	private MessageObject handleServerGetRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	private MessageObject handleServerPutRequest() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean pollServerForWriteAgreement(MessageObject message, Integer serverId) {
		boolean result = false;
		
		Socket commPort = null;
		ObjectInputStream reader = null;
		ObjectOutputStream writer = null; 
		
		try {
			commPort = new Socket(Main.getAllNodes().get(serverId).getHostName(), Main.getAllNodes().get(serverId).getPortNo());
			writer = new ObjectOutputStream (commPort.getOutputStream());
			reader = new ObjectInputStream (commPort.getInputStream());
			
			writer.writeObject(message);
			
			MessageObject returnMessage = (MessageObject) reader.readObject();
			
			//If server agrees to write this value.
			if (returnMessage.getMessageType() == MessageType.SERVER_TO_SERVER_PUT_OK) {
				result = true;
			}	
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.err);
				}
			}				
			if (commPort != null) {
				try {
					commPort.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.err);
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.err);
				}
			}			
		}
		return result;
		
	}

	private MessageObject handleClientPutRequest(ContentObject objectParam) {
		// TODO Auto-generated method stub
		boolean putResultOk = false;
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
				putResultOk = pollServerForWriteAgreement(message, primaryHashValue) ;
			}
			
			if (secondHashvalue != this.nodeId) {
				putResultOk = putResultOk || pollServerForWriteAgreement(message, secondHashvalue) ;
			}
			
			if (tertiaryHashvalue != this.nodeId) {
				putResultOk = putResultOk || pollServerForWriteAgreement(message, tertiaryHashvalue) ;
			}
			
			if (putResultOk) {
				
			}
		}
		MessageObject newMessage = new MessageObject();
		
		if (putResultOk) {
			newMessage.setMessageType(MessageType.SERVER_TO_CLIENT_PUT_OK);
		}
		return newMessage;
		
	}

	private MessageObject handleClientGetRequest(ContentObject objectParam) {
		// TODO Auto-generated method stub
		
		ContentObject returnObject = null;

		if (objectParam != null) {
			Integer key = objectParam.getObjId();
			
			Integer primaryHashValue = (key % Constant.hashBase );
			Integer secondHashvalue = (primaryHashValue + 1) % Constant.hashBase ;
			Integer tertiaryHashvalue = (secondHashvalue + 1) % Constant.hashBase ;
			
			if (primaryHashValue.equals(nodeId)) {
				returnObject = this.primaryObjects.get(key); 
			} else if (secondHashvalue.equals(nodeId)) {
				returnObject = this.secondaryObjects.get(key); 
			} else if (tertiaryHashvalue.equals(key)){
				returnObject = this.tertiaryObjects.get(key);
			}
		}

		MessageObject newMessage = new MessageObject();
		
		newMessage.setMessageType(MessageType.SERVER_TO_CLIENT_READ_OK);
		newMessage.setContentObject(returnObject);
		
		return newMessage;
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
