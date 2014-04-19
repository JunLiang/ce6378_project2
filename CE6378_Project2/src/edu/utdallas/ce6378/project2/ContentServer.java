package edu.utdallas.ce6378.project2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


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
	
	
	
	//Nodes that being cut off from current node.
	HashSet<Integer> cutOffNodes;
	
	/*save the direct connections to other servers to 
	 * initiate server to server communications.
	 */
	/*private TreeMap<Integer,ObjectOutputStream> outboundPipes;
	private TreeMap<Integer, Socket> outboundSockets;*/
	
	public ContentServer(NodeConfiguration localNodeConfig) {
		// TODO Auto-generated constructor stub
		config = localNodeConfig;
		nodeId = localNodeConfig.getNodeId();
		logicTimestamp = new VectorTimestamp(Constant.numberOfServers);		
		primaryObjects = new ConcurrentHashMap<Integer, ContentObject> ();
		secondaryObjects = new ConcurrentHashMap<Integer, ContentObject> ();
		tertiaryObjects = new ConcurrentHashMap<Integer, ContentObject> ();
		serverSockets = new TreeMap<Integer, Socket> ();
		/*outboundPipes = new TreeMap<Integer,ObjectOutputStream>();
		outboundSockets = new TreeMap<Integer, Socket>();*/
		cutOffNodes = new HashSet<Integer>();
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
			
			boolean terminate = false;
			try {
				in = new ObjectInputStream(commPort.getInputStream());
				out = new ObjectOutputStream (commPort.getOutputStream());
				
				while (!terminate) {
					try {
							MessageObject oMessage = (MessageObject)in.readObject();
							
							if (oMessage != null && getServerStatus() != ServerStatus.SERVER_FAIL) {
								/*Incoming message, let's adjust my clock*/
								tickMyClock();
								adjustMyClock(oMessage.getTimestamp());
							}
							
							if (oMessage.getMessageType() == MessageType.SERVER_TO_SERVER_PUT_END) {
								//This is a connection established between servers for 
								//write synchronization. Now write finished, terminate.
								//Do not reply anything here, even unavailable message is not needed.
								terminate = true;
							} else {
							
								MessageObject returnMessage = handleIncomeMessage(oMessage);
								
								if (returnMessage != null) {
									//bump up time stamp
									if (serverStatus != ServerStatus.SERVER_FAIL) {
										//If server did not fail, then tick the clock.
										tickMyClock();
									}
									returnMessage.setTimestamp(getLogicTimestamp());
									returnMessage.setFromServerId(getNodeId());
									
									out.writeObject(returnMessage);
							}
						}
					}catch (EOFException eofex) {
						//Ignore the EOFException for now.	
						//eofex.printStackTrace(System.err);
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
				
				if (out != null ) {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (commPort != null) {
					try {
						commPort.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
		
	public MessageObject handleIncomeMessage(MessageObject oMessage) {
		//Handle messages 
		
		MessageType messageType = oMessage.getMessageType();
		MessageObject returnMessage = null;
		
		if (this.serverStatus == ServerStatus.SERVER_FAIL) {
			
			returnMessage = new MessageObject();
			returnMessage.setFromServerId(nodeId);
			returnMessage.setMessageType(MessageType.SERVER_UNAVAILABLE);
			
		} else {			
	 		
			switch (messageType) { 
				
				case CLIENT_GET_OBJECT :returnMessage = handleClientGetRequest(oMessage.getContentObject()); break;
				
				case CLIENT_PUT_OBJECT :returnMessage = handleClientPutRequest(oMessage.getContentObject()); break;
				
				case SERVER_PUT_OBJECT :returnMessage = handleServerPutRequest(oMessage.getContentObject()); break;
				
				//Handle this request type later, as crash recover is not yet required.
				case SERVER_GET_OBJECTS :returnMessage = handleServerGetRequest(); break;
				
				//Server FAILURE simulation.
				case SERVER_CONTROL_FAIL:returnMessage = handleSimulationFail(); break;
				
				case SERVER_CONTROL_ISOLATE : returnMessage = handleServerIsolate(oMessage.getFromServerId()); break;
				
				default : break;
			}		
		}		
		
		return returnMessage;
	}

	private synchronized MessageObject handleServerIsolate(Integer fromServerId) {
		// TODO Auto-generated method stub
		//synchronized to make sure isolation is in sync 
		if (fromServerId != -1) {
			this.cutOffNodes.add(fromServerId);
		}
		MessageObject newMessage = new MessageObject();
		newMessage.setFromServerId(getNodeId());
		newMessage.setMessageType(MessageType.SERVER_CUT_OFF_OK);
		//newMessage.setTimestamp(logicTimestamp);
		newMessage.setContentObject(null);
		return newMessage;
	}
	
	//Synchronized to change server status.
	private synchronized MessageObject handleSimulationFail() {
		// TODO Auto-generated method stub
		MessageObject newMessage = new MessageObject();
		
		
		this.serverStatus = ServerStatus.SERVER_FAIL;
		
		
		newMessage.setFromServerId(nodeId);
		newMessage.setMessageType(MessageType.SERVER_UNAVAILABLE);
		//newMessage.setTimestamp(getLogicTimestamp());
		newMessage.setContentObject(null);
		
		return newMessage;
		
	}

	private MessageObject handleServerGetRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	private MessageObject handleServerPutRequest(ContentObject contentObject) {
		// TODO Auto-generated method stub
		MessageObject newMessage = new MessageObject();
		newMessage.setFromServerId(nodeId);
		//newMessage.setTimestamp(logicTimestamp);
		newMessage.setMessageType(MessageType.SERVER_TO_SERVER_PUT_FAIL);
		
		if (contentObject != null) {
			Integer key = contentObject.getObjId();
			Integer primaryHashValue = (key % 7 );
			Integer secondHashvalue = (primaryHashValue + 1) % 7;
			Integer tertiaryHashvalue = (secondHashvalue + 1) % 7;
			
			ConcurrentHashMap<Integer, ContentObject> container = null;
			
			if (primaryHashValue.equals(nodeId)) {
				container = this.primaryObjects;
			} else if ( secondHashvalue.equals(nodeId)) {
				container = this.secondaryObjects;
			} else if ( tertiaryHashvalue.equals(nodeId)) {
				container = this.tertiaryObjects;
			}
			
			if (container != null) {
				ContentObject obj = container.get(key);
				
				if (obj == null || obj.getTimestamp().compareTo(contentObject.getTimestamp()) < 0) {
					System.out.println("Server "+ nodeId + " store object " + contentObject.getObjId() + " at time " + this.logicTimestamp.printTimestamp());
					container.put(key, contentObject);
				}
				
				newMessage.setMessageType(MessageType.SERVER_TO_SERVER_PUT_OK);
			}
		}
		
		return newMessage;
	}
	
	private boolean pollServerForWriteAgreement(MessageObject message, Integer serverId) {
		
		boolean result = false;
		
		Socket commPort = null;
		ObjectInputStream reader = null;
		ObjectOutputStream writer = null; 
		
		try {
			this.tickMyClock();
			
			//Create a SERVER to SERVER connection (this connection is not needed to be permanent, 
			//Once the message exchange finished, bring it down.
			
			commPort = new Socket(Main.getAllServerNodes().get(serverId).getHostName(), Main.getAllServerNodes().get(serverId).getPortNo());
			writer = new ObjectOutputStream (commPort.getOutputStream());
			reader = new ObjectInputStream (commPort.getInputStream());
			
			writer.writeObject(message);
			
			MessageObject returnMessage = (MessageObject) reader.readObject();
			
			if (returnMessage.getMessageType() != MessageType.SERVER_UNAVAILABLE) {
				adjustMyClock(returnMessage.getTimestamp());
			}
			
			//If server agrees to write this value.
			if (returnMessage.getMessageType() == MessageType.SERVER_TO_SERVER_PUT_OK) {
				System.out.println("Server "+ nodeId + " polled server " + serverId + " at time " + this.logicTimestamp.printTimestamp()+ " for writing object " + message.getContentObject().getObjId() + " results ok.");
				result = true;
			} else {
				System.out.println("Server "+ nodeId + " polled server " + serverId + " at time " + this.logicTimestamp.printTimestamp()+ " for writing object " + message.getContentObject().getObjId() + " results failed.");
			}
			
			this.tickMyClock();
			
			MessageObject newMessage = new MessageObject();
			newMessage.setFromServerId(getNodeId());
			newMessage.setMessageType(MessageType.SERVER_TO_SERVER_PUT_END);
			newMessage.setTimestamp(getLogicTimestamp());
			newMessage.setContentObject(null);
			
			//Send this connection the termination message,
			//so the other server does not have to busy wait anymore.
			writer.writeObject(newMessage);
			
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
		boolean putResultPOk = false;
		boolean putResultSOk = false;
		boolean putResultTOk = false;
		if (objectParam != null) {
			Integer key = objectParam.getObjId();
			
			Integer primaryHashValue = (key % Constant.hashBase );
			Integer secondHashvalue = (primaryHashValue + 1) % Constant.hashBase;
			Integer tertiaryHashvalue = (secondHashvalue + 1) % Constant.hashBase;
			
			MessageObject message = new MessageObject();
			
			Integer[] timeVector = new Integer [] {this.logicTimestamp.getTimeVector()[primaryHashValue], this.logicTimestamp.getTimeVector()[secondHashvalue], this.logicTimestamp.getTimeVector()[tertiaryHashvalue]};
			
			objectParam.getTimestamp().setTimeVector(timeVector);
			
			message.setContentObject(objectParam);
			message.setFromServerId(nodeId);
			message.setMessageType(MessageType.SERVER_PUT_OBJECT);
			
			//TODO, need a non blocking way to send the write request 
			//to other severs and wait for an answer.
			ConcurrentHashMap<Integer, ContentObject> container = null;
			if (primaryHashValue != this.nodeId) {
				if (!this.cutOffNodes.contains(primaryHashValue)) {
					putResultPOk = pollServerForWriteAgreement(message, primaryHashValue) ;
				}
			} else {
				container = this.primaryObjects;
			}
			
			if (secondHashvalue != this.nodeId) {
				//Preventing the logic operator short circuiting, use an additional logic variable 
				if (!this.cutOffNodes.contains(secondHashvalue)) {
					putResultSOk = pollServerForWriteAgreement(message, secondHashvalue) ;
				}
			} else {
				container = this.secondaryObjects;
			}
			
			if (tertiaryHashvalue != this.nodeId) {
				//Preventing the logic operator short circuiting, use an additional logic variable
				if (!this.cutOffNodes.contains(tertiaryHashvalue)) {
					putResultTOk =  pollServerForWriteAgreement(message, tertiaryHashvalue) ;
				}
			} else {
				container = this.tertiaryObjects;
			}
			
			if (putResultPOk || putResultSOk || putResultTOk) {
				if (container.get(key) == null || container.get(key).getTimestamp().compareTo(objectParam.getTimestamp()) < 0 ) {
					System.out.println("Server "+ nodeId + " store object " + objectParam.getObjId() + " at time " + this.logicTimestamp.printTimestamp());
					container.put(key, objectParam);
				}
			} else {
				System.out.println("Server "+ nodeId + " can not store object " + objectParam.getObjId() + " at time " + this.logicTimestamp.printTimestamp() + " as consensus can not be reached.");
			}
		}
		
		MessageObject newMessage = new MessageObject();
		newMessage.setFromServerId(nodeId);
		newMessage.setContentObject(objectParam);
		if (putResultPOk || putResultSOk || putResultTOk) {
			newMessage.setMessageType(MessageType.SERVER_TO_CLIENT_PUT_OK);			
		} else {
			newMessage.setMessageType(MessageType.SERVER_TO_CLIENT_PUT_FAIL);			
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
			} else if (tertiaryHashvalue.equals(nodeId)){
				returnObject = this.tertiaryObjects.get(key);
			}
		}

		MessageObject newMessage = new MessageObject();
		
		newMessage.setMessageType(MessageType.SERVER_TO_CLIENT_READ_OK);
		//newMessage.setTimestamp(getLogicTimestamp());
		newMessage.setContentObject(returnObject);
		newMessage.setFromServerId(getNodeId());
		
		return newMessage;
	}
	
	/*private void establishConnectionToServers() throws UnknownHostException, IOException {
		//save all the connections to the other servers 
		for (NodeConfiguration aConfig : Main.getAllNodes()) {
			if (aConfig.getNodeId() != this.config.getNodeId()) {
				Socket socket = new Socket(aConfig.getHostName(), aConfig.getPortNo());
				this.outboundSockets.put(aConfig.getNodeId(), socket);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				this.outboundPipes.put(aConfig.getNodeId(), out);
			}
		}
	}*/


	public void start() throws UnknownHostException, IOException {
		// Start Listener thread
		ListenerThread listener = new ListenerThread(config.getPortNo());
		
		Thread listenerThread = new Thread(listener);
		
		listenerThread.start();
				
		//establishConnectionToServers();	
	}

}
