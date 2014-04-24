package edu.utdallas.ce6378.project2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;


public class ContentServer {
	
	private Integer nodeId;
	
	//Content stored on each server.
	
	private ConcurrentSkipListMap<Integer, ContentObject> objectsStorage;
	//the primary secondary and tertiary objects are 
	//used as buffers of write operations.
	private ConcurrentSkipListMap<Integer, TreeMap<VectorTimestamp, ContentObject> > bufferStorage;

	
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
		objectsStorage = new ConcurrentSkipListMap<Integer, ContentObject> ();
		bufferStorage = new ConcurrentSkipListMap<Integer, TreeMap<VectorTimestamp, ContentObject> >();

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

	public ConcurrentSkipListMap<Integer, TreeMap<VectorTimestamp, ContentObject> > getPrimaryObjects() {
		return bufferStorage;
	}

	public void setPrimaryObjects(
			ConcurrentSkipListMap<Integer, TreeMap<VectorTimestamp, ContentObject> > primaryObjects) {
		this.bufferStorage = primaryObjects;
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
				
				case SERVER_COMMIT_OBJECT :returnMessage = handleServerCommitRequest(oMessage.getContentObject()); break;
				
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

	private MessageObject handleServerCommitRequest(ContentObject contentObject) {
		MessageObject newMessage = new MessageObject();
		newMessage.setFromServerId(nodeId);
		//newMessage.setTimestamp(logicTimestamp);
		newMessage.setMessageType(MessageType.SERVER_TO_SERVER_COMMIT_OK);
		newMessage.setContentObject(null);
		
		if (contentObject != null) {			
			this.commitWriteOperation(contentObject);			
		}
		
		return newMessage;
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
	
	private synchronized void commitWriteOperation(ContentObject thisObject) {
		//This is the part where the buffer gets flushed into the storage
		
		VectorTimestamp timestamp = thisObject.getTimestamp();
		Integer key = thisObject.getObjId();
		
		/*Flushing the buffered versions of the same object id and 
		 * time stamp <= thisObject's timestamp
		 */
		TreeMap<VectorTimestamp, ContentObject> objBuffer = this.bufferStorage.get(key);
		
			if (objBuffer != null) {
				for (Iterator<Map.Entry<VectorTimestamp, ContentObject> > it = objBuffer.entrySet().iterator(); it.hasNext();) {
					ContentObject object = it.next().getValue();
					if (object != null) {
						VectorTimestamp histTimestamp = object.getTimestamp();
						Integer primaryHashValue = key % Constant.hashBase;
						Integer secondaryHashValue = (primaryHashValue + 1) % Constant.hashBase;
						Integer tertiaryHashValue = (secondaryHashValue + 1) % Constant.hashBase;
						
						/*if ( timestamp.getTimeVector()[0] < this.logicTimestamp.getTimeVector()[primaryHashValue] &&
								timestamp.getTimeVector()[1] < this.logicTimestamp.getTimeVector()[secondaryHashValue] &&
								timestamp.getTimeVector()[2] < this.logicTimestamp.getTimeVector()[tertiaryHashValue]) {*/
						if (histTimestamp.compareTo(timestamp) <= 0) {							
							this.objectsStorage.put(key, object);
							System.out.println("Server "+this.nodeId + " store " + object.printContentObject() + " at time " + this.logicTimestamp.printTimestamp());							
							it.remove();
						}
					}
				}
			}
			
			if (objBuffer != null && objBuffer.size() == 0) {
				this.bufferStorage.remove(key);
			}
		
		/*for (Iterator< Map.Entry<VectorTimestamp, HashMap<Integer, ContentObject>>> it = this.bufferStorage.entrySet().iterator(); it.hasNext();) {
			HashMap<Integer, ContentObject> objBuffer = it.next().getValue();
			if (objBuffer.size() == 0) {
				it.remove();
			}
		}*/
	}
	
	//Add the pending object to the buffer, and it will be flushed when the write commits.
	private synchronized void addContentObjectToBuffer(ContentObject contentObject) {
		TreeMap<VectorTimestamp, ContentObject> objBuffer = this.bufferStorage.get(contentObject.getObjId());
		
		if (objBuffer == null) {
			objBuffer = new TreeMap<VectorTimestamp, ContentObject>();
			this.bufferStorage.put(contentObject.getObjId(), objBuffer);
		}
		System.out.println("Server "+ nodeId + " buffer object " + contentObject.printContentObject() + " at time " + this.logicTimestamp.printTimestamp());
		objBuffer.put(contentObject.getTimestamp(), contentObject);
		
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
			
			//ConcurrentSkipListMap<Integer, TreeMap<VectorTimestamp, ContentObject> > container = this.bufferStorage;
			
			/*if (primaryHashValue.equals(nodeId)) {
				container = this.bufferStorage;
			} else if ( secondHashvalue.equals(nodeId)) {
				container = this.secondaryObjects;
			} else if ( tertiaryHashvalue.equals(nodeId)) {
				container = this.tertiaryObjects;
			}*/
			
			//if (container != null) {
				//ContentObject obj = container.get(key);
				/*TreeMap<VectorTimestamp, ContentObject> objBuffer = container.get(contentObject.getObjId());
				
				if (objBuffer == null) {
					objBuffer = new TreeMap<VectorTimestamp, ContentObject>();
					container.put(contentObject.getObjId(), objBuffer);
				}
				System.out.println("Server "+ nodeId + " buffer object " + contentObject.printContentObject() + " at time " + this.logicTimestamp.printTimestamp());
				objBuffer.put(contentObject.getTimestamp(), contentObject);*/

				/*if (obj == null || obj.getTimestamp().compareTo(contentObject.getTimestamp()) < 0) {
					System.out.println("Server "+ nodeId + " store object " + contentObject.getObjId() + " at time " + this.logicTimestamp.printTimestamp());
					//container.put(key, contentObject);
					this.commitWriteOperation(container, contentObject);
				}*/
				
			this.addContentObjectToBuffer(contentObject);
			newMessage.setMessageType(MessageType.SERVER_TO_SERVER_PUT_OK);
			//}
		}
		
		return newMessage;
	}

	private boolean pollServerForWriteCommit(MessageObject message, Integer serverId) {
		
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
			if (returnMessage.getMessageType() == MessageType.SERVER_TO_SERVER_COMMIT_OK) {
				System.out.println("Server "+ nodeId + " polled server " + serverId + " at time " + this.logicTimestamp.printTimestamp()+ " for commit object " + message.getContentObject().getObjId() + " results ok.");
				result = true;
			} else {
				System.out.println("Server "+ nodeId + " polled server " + serverId + " at time " + this.logicTimestamp.printTimestamp()+ " for commit object " + message.getContentObject().getObjId() + " results failed.");
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
		
		Integer serverMessagesExchanged = 0;
		
		if (objectParam != null) {
			Integer key = objectParam.getObjId();
			
			Integer primaryHashValue = (key % Constant.hashBase );
			Integer secondHashvalue = (primaryHashValue + 1) % Constant.hashBase;
			Integer tertiaryHashvalue = (secondHashvalue + 1) % Constant.hashBase;
			
			if (primaryHashValue.equals(nodeId) || secondHashvalue.equals(nodeId) || tertiaryHashvalue.equals(nodeId) ) {
				
				//Set the object time stamp based on current logical clock values.

				Integer[] timeVector = new Integer [] {this.logicTimestamp.getTimeVector()[primaryHashValue], this.logicTimestamp.getTimeVector()[secondHashvalue], this.logicTimestamp.getTimeVector()[tertiaryHashvalue]};				
				objectParam.getTimestamp().setTimeVector(timeVector);
				
				//TODO, need a non blocking way to send the write request 
				//to other severs and wait for an answer.
				/*ConcurrentSkipListMap<Integer, TreeMap<VectorTimestamp, ContentObject> > container = this.bufferStorage;				

				System.out.println("Server "+ nodeId + " buffer object " + objectParam.printContentObject() + " at time " + this.logicTimestamp.printTimestamp());
				//container.put(key, objectParam);
				//add write request to buffer. Commit write only when the current time stamp is larger.

				TreeMap<VectorTimestamp, ContentObject> objBuffer = container.get(key);
				if (objBuffer == null) {
					objBuffer = new TreeMap<VectorTimestamp, ContentObject> ();
					container.put(key, objBuffer);
				}
				objBuffer.put(objectParam.getTimestamp(), objectParam);*/
				
				this.addContentObjectToBuffer(objectParam);
				
				//Only process object that can be stored by this server.
			
				MessageObject message = new MessageObject();
								
				message.setContentObject(objectParam);
				message.setFromServerId(nodeId);
				message.setMessageType(MessageType.SERVER_PUT_OBJECT);
				
							
				if (tertiaryHashvalue != this.nodeId) {
					//Preventing the logic operator short circuiting, use an additional logic variable
					if (!this.cutOffNodes.contains(tertiaryHashvalue)) {
						putResultTOk =  pollServerForWriteAgreement(message, tertiaryHashvalue) ;
						serverMessagesExchanged +=2;
					}
				} /*else {
					container = this.tertiaryObjects;
				}*/
				
				if (secondHashvalue != this.nodeId) {
					//Preventing the logic operator short circuiting, use an additional logic variable 
					if (!this.cutOffNodes.contains(secondHashvalue)) {
						putResultSOk = pollServerForWriteAgreement(message, secondHashvalue) ;
						serverMessagesExchanged +=2;
					}
				} /*else {
					container = this.secondaryObjects;
				}*/

				if (primaryHashValue != this.nodeId) {
					if (!this.cutOffNodes.contains(primaryHashValue)) {
						putResultPOk = pollServerForWriteAgreement(message, primaryHashValue) ;
						serverMessagesExchanged +=2;
					}
				} /*else {
					container = this.bufferStorage;
				}*/
				
				if (putResultPOk || putResultSOk || putResultTOk) {
					MessageObject commitMessage = new MessageObject();
					commitMessage.setMessageType(MessageType.SERVER_COMMIT_OBJECT);
					commitMessage.setContentObject(objectParam);
					commitMessage.setFromServerId(nodeId);
					
					if (tertiaryHashvalue != this.nodeId) {
						//Preventing the logic operator short circuiting, use an additional logic variable
						if (!this.cutOffNodes.contains(tertiaryHashvalue)) {
							pollServerForWriteCommit(commitMessage, tertiaryHashvalue) ;
							serverMessagesExchanged +=2;
						}
					}
					
					if (secondHashvalue != this.nodeId) {
						//Preventing the logic operator short circuiting, use an additional logic variable 
						if (!this.cutOffNodes.contains(secondHashvalue)) {
							pollServerForWriteCommit(commitMessage, secondHashvalue) ;
							serverMessagesExchanged +=2;
						}
					} 

					if (primaryHashValue != this.nodeId) {
						if (!this.cutOffNodes.contains(primaryHashValue)) {
							pollServerForWriteCommit(commitMessage, primaryHashValue) ;
							serverMessagesExchanged +=2;
						}
					} 
					
					commitWriteOperation(objectParam);
					
				} else {
					System.out.println("Server "+ nodeId + " can not store object " + objectParam.getObjId() + " at time " + this.logicTimestamp.printTimestamp() + " as consensus can not be reached.");
				}
			}
		}
		
		MessageObject newMessage = new MessageObject();
		newMessage.setFromServerId(nodeId);
		newMessage.setContentObject(objectParam);
		newMessage.setServerMessagesExchanged(serverMessagesExchanged);
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
			
			/*Integer primaryHashValue = (key % Constant.hashBase );
			Integer secondHashvalue = (primaryHashValue + 1) % Constant.hashBase ;
			Integer tertiaryHashvalue = (secondHashvalue + 1) % Constant.hashBase ;*/
			
			/*if (primaryHashValue.equals(nodeId)) {
				returnObject = this.primaryObjects.get(key); 
			} else if (secondHashvalue.equals(nodeId)) {
				returnObject = this.secondaryObjects.get(key); 
			} else if (tertiaryHashvalue.equals(nodeId)){
				returnObject = this.tertiaryObjects.get(key);
			}*/
			returnObject = this.objectsStorage.get(key);
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
