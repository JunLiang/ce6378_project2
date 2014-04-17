package edu.utdallas.ce6378.project2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class CommExpriment {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CommExpriment expr = new CommExpriment();
		
		expr.simulation();
		

	}
	
	public CommExpriment () {
	}
	
	public void simulation () {
		Thread listener = new Thread(new Listener(8888));
		Thread client = new Thread (new ClientLogicThread (8888));
		
		listener.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		client.start();
	}
	
	class Listener implements Runnable {
		private Integer serverPortNo;
		
		public Listener (Integer portNo) {
			
			this.serverPortNo = portNo;
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ServerSocket serverSocket = null;
			try {				
				serverSocket = new ServerSocket (serverPortNo);
				
				while (true ) {
					Socket commPort = serverSocket.accept();
					ServerLogicThread serverLogic = new  ServerLogicThread(commPort);
					Thread serverThread = new Thread(serverLogic);
					serverThread.start();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.err);
				}
			}
			
		}
		
	}
	
	class ServerLogicThread implements Runnable {
		private Socket commPort;

		public ServerLogicThread (Socket commPort) {
			this.commPort = commPort;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ObjectInputStream reader = null;
			ObjectOutputStream writer = null; 
			Random rand2 = new Random();
			try{				
				writer = new ObjectOutputStream (this.commPort.getOutputStream());
				reader = new ObjectInputStream (this.commPort.getInputStream());
				Integer round = 0;
				while (round < 101) {
					try {
						
						System.out.println("Round: "+ round);
						MessageObject value = (MessageObject) reader.readObject();
						
						System.out.println("Server Read 1 " + value.getMessageType() + " Int value is "+ value.getContentObject().getIntValue());
						
						value.setMessageType(MessageType.SERVER_TO_CLIENT_READ_OK);
						value.getContentObject().setStrValue("Abcd"+rand2.nextInt(1000));
						writer.writeObject(value);
						
						Integer intValue = (Integer) reader.readObject();
						System.out.println("Server Read 2 " + intValue);
						writer.writeObject(round);
						
						round++;
						writer.reset();
					}
					catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(System.err);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.err);
			}  finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(System.err);
					}
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(System.err);
					}
				}
				if (this.commPort != null) {
					try {
						this.commPort.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(System.err);
					}
				}
			}
			
		}		
	}
	
	class ClientLogicThread implements Runnable {
		private Integer serverPortNo;
		public ClientLogicThread (Integer serverPortNo)  {
			this.serverPortNo = serverPortNo;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Socket commPort = null;
			ObjectInputStream reader = null;
			ObjectOutputStream writer = null;
			Random randGen = new Random();
			try {
				commPort = new Socket ("localhost", this.serverPortNo);
				writer = new ObjectOutputStream (commPort.getOutputStream());
				reader = new ObjectInputStream (commPort.getInputStream());
				Integer round = 0;
				MessageObject newMessage = new MessageObject();
				ContentObject newContent = new ContentObject();
				
				newMessage.setMessageType(MessageType.CLIENT_GET_OBJECT);
				newMessage.setContentObject(newContent);
				newContent.setObjId(randGen.nextInt(10000));
				
				while (round < 100) {
					
					
					Integer value;
					try {
						value = randGen.nextInt(10000);
						//System.out.println("client random value is "+value);
						
						newMessage.getContentObject().setIntValue(value);
						
						System.out.println("Client: The message object content int value is now "+newMessage.getContentObject().getIntValue());
						
						
						writer.writeObject(newMessage);
						
						MessageObject oldMessage = (MessageObject) reader.readObject();
						
						System.out.println("Client 1: " + oldMessage.getMessageType() + " Str value " + oldMessage.getContentObject().getStrValue());
						
						writer.writeObject(randGen.nextInt(10000));
						round = (Integer) reader.readObject();
						System.out.println("Client 2 read round value : " + round);
						
						writer.reset();
						
					} catch (EOFException eofex) {
						eofex.printStackTrace(System.err);
					}
					catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.err);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(System.err);
					}
				}
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
			}
		}
	}

}
