package edu.utdallas.ce6378.project2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader; 
import java.lang.Math;

//import edu.utdallas.ce6378.project2.Constant;

//
public class InteractiveClient {
	
	HashMap<Integer, Socket> serverSockets ;
	HashMap<Integer, ObjectOutputStream> writeToServerPipes;
	HashMap<Integer, ObjectInputStream> readFromServerPipes;

	
	Integer nodeId; //client node Id;
	
	public InteractiveClient (NodeConfiguration localNodeConfig) {		
		nodeId = localNodeConfig.getNodeId();
		serverSockets = new HashMap<Integer, Socket> ();
		writeToServerPipes = new HashMap<Integer, ObjectOutputStream> ();
		readFromServerPipes = new HashMap<Integer, ObjectInputStream> ();		
	}

	private void establishConnectionToServers() throws UnknownHostException, IOException {
		//save all the connections to the other servers 
		for (NodeConfiguration aConfig : Main.getAllServerNodes().values()) {			
			Socket socket = new Socket(aConfig.getHostName(), aConfig.getPortNo());
			this.serverSockets.put(aConfig.getNodeId(), socket);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			this.writeToServerPipes.put(aConfig.getNodeId(), out);
			
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			this.readFromServerPipes.put(aConfig.getNodeId(), in);
		}
	}
	
	public void simulate() {
		//Wait for all server to come online first
		//sleep for a few seconds here.
		
		try {
			Thread.sleep(6000);
			establishConnectionToServers();
			
			Thread clientThread = new Thread(new ClientThread());
			
			clientThread.start();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			/*for (ObjectOutputStream out : this.writeToServerPipes.values()) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for (ObjectInputStream in : this.readFromServerPipes.values()) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for (Socket socket : this.serverSockets.values()) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
		}	
		
	}
	
	class ClientThread implements Runnable {
		private String Cli_Server_Index = null;
		private String Cli_Operation_Type = null;
		private Integer Cli_Object_Key = -1;
		private Integer Cli_Object_Value = -1;
		private Integer Cli_Total_Num_Put_Request = -1;
		private Integer writeServerId = -1;
		private Integer HashSocketKey = -1;
		private Integer round = 0;
		public ClientThread() {
			
		}

		@Override
		public void run() {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));  
			String line = null;	
			Random randGen = new Random();
			
			try {
				//Create a TCP/IP connection between the client and each server
					while(true)
					{
						System.out.print("User Input: ");  
						line = reader.readLine();  
						
						String[] fields = line.split(",");

						//parse input command line
						if (4 == fields.length) 
						{
							Cli_Server_Index 	= fields[0].trim();
							Cli_Operation_Type	= fields[1].trim();
							if(Cli_Operation_Type.equals("put")) //put operation
							{
								Cli_Object_Key		= Integer.valueOf(fields[2].trim());
								Cli_Object_Value	= Integer.valueOf(fields[3].trim());
							}
							else if(Cli_Operation_Type.equals("wreq")) //generate write request operation
							{
								Cli_Object_Key		= Integer.valueOf(fields[2].trim());
								Cli_Total_Num_Put_Request = Integer.valueOf(fields[3].trim());
								System.out.println(" Cli_Server_Index=" + Cli_Server_Index + " Cli_Operation_Type=" + Cli_Operation_Type + " Cli_Object_Key="+ Cli_Object_Key + " Cli_Total_Num_Put_Request=" + Cli_Total_Num_Put_Request);
							}
							else
							{
								Cli_Operation_Type = null;
							
								System.out.println("Cli_Operation_Type input has error! ");
							}
						}
						else if(3 == fields.length) 
						{
							Cli_Server_Index 	= fields[0].trim();
							Cli_Operation_Type	= fields[1].trim();
							Cli_Object_Key		= Integer.valueOf(fields[2].trim());
							System.out.println(" Cli_Server_Index=" + Cli_Server_Index + " Cli_Operation_Type=" + Cli_Operation_Type + " Cli_Object_Key="+ Cli_Object_Key);
						}
						else
						{
							Cli_Operation_Type = null;
							System.out.println("User input has error! ");
						}
						
					//	System.out.println("[debug]Cli_Server_Index=" + Cli_Server_Index + "  Cli_Operation_Type=" + Cli_Operation_Type + "  Cli_Object_Key=" + Cli_Object_Key + "  Cli_Object_Value= " + Cli_Object_Value); 
						
						if(Cli_Server_Index.equals("a"))  //Primary server
						{
						
							writeServerId = Cli_Object_Key % 7;
						}
						else if(Cli_Server_Index.equals("b"))
						{
							writeServerId = (Cli_Object_Key + 1) % 7;
						}
						else if(Cli_Server_Index.equals("c"))
						{
							writeServerId = (Cli_Object_Key + 2) % 7;
						}
						else
						{
							System.out.println("\n Unknown client operation type ");
						}
						if(Cli_Operation_Type == null)
						{
							System.out.println("[debug]Cli_Operation_Type is null");
						}
						else
						{
					    	if(Cli_Operation_Type.equals("put") || Cli_Operation_Type.equals("get")) 
						    {
								
								if(Cli_Operation_Type.equals("put"))  //Insert/Write an object to a server
								{
									System.out.println("[debug]Cli_Operation_Type is put");
									
									//Build message and send it to server
									MessageObject newMessage = new MessageObject();
									MessageObject RecvMessage = new MessageObject();
									ContentObject newContent = new ContentObject();
									
									newMessage.setMessageType(MessageType.CLIENT_PUT_OBJECT);
									newMessage.setContentObject(newContent);
									newContent.setObjId(Cli_Object_Key);
									
									newContent.setIntValue(Cli_Object_Value);
								
									writeToServerPipes.get(writeServerId).writeObject(newMessage);

									System.out.println("\n [debug]writeServerId = " + writeServerId);
									System.out.println("\n [debug]Client send msg: " + " MsgType=" + newMessage.getMessageType() + " ObjId=" + newMessage.getContentObject().getObjId() + " ObjValue=" + newMessage.getContentObject().getIntValue());
									
									try {
										RecvMessage = (MessageObject)readFromServerPipes.get(writeServerId).readObject();
									} catch (ClassNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}	
									
									System.out.println("\n [debug]Client recv msg: " + " MsgType=" + RecvMessage.getMessageType());
									
								}
								else if(Cli_Operation_Type.equals("get")) //Read an object from a server
								{
									System.out.println("[debug]Cli_Operation_Type is Get");
									
									//Build message and send it to server
									MessageObject newMessage = new MessageObject();
									ContentObject newContent = new ContentObject();
									MessageObject RecvMessage = new MessageObject();
									newMessage.setMessageType(MessageType.CLIENT_GET_OBJECT);
									newMessage.setContentObject(newContent);
									newContent.setObjId(Cli_Object_Key);
									System.out.println("[debug]writeServerId = " + writeServerId);
									writeToServerPipes.get(writeServerId).writeObject(newMessage);
									System.out.println("\n [debug]Client send msg: " + " MsgType=" + newMessage.getMessageType() + " ObjId=" + newMessage.getContentObject().getObjId());
									
									try {
										RecvMessage = (MessageObject)readFromServerPipes.get(writeServerId).readObject();
									} catch (ClassNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}	
									if(RecvMessage.getContentObject() == null)
									{
										System.out.println("\n [debug]Client recv msg: " + " MsgType= " + RecvMessage.getMessageType());
										System.out.println("\n Get operation result is " + "Object does NOT exist");
										
									}
									else
									{
										System.out.println("\n [debug]Client recv msg: " + " MsgType= " + RecvMessage.getMessageType() + " ObjValue=" + RecvMessage.getContentObject().getIntValue());
										System.out.println("\n Get operation result is" + " ObjId=" + Cli_Object_Key + " ObjValue=" + RecvMessage.getContentObject().getIntValue());
									}
								}
						    }
							else if(Cli_Operation_Type.equals("wreq")) //write the same object concurrently by different clients
							{
								//System.out.println("enter into wreq implement branch ");
								//command format: server_id,wreq,obj_id,TimesofRequest
								round = 0;
								while (round < Cli_Total_Num_Put_Request)
								{
									System.out.println("wreq implement branch ");
									round++;
									//Cli_Object_Value = randGen.nextInt(Constant.objectKeyRange);
									Cli_Object_Value = randGen.nextInt(Constant.objectValueRange);
									try {
										//Build message and send it to server
										MessageObject newMessage = new MessageObject();
										MessageObject RecvMessage = new MessageObject();
										ContentObject newContent = new ContentObject();
								
										newMessage.setMessageType(MessageType.CLIENT_PUT_OBJECT);
										newMessage.setContentObject(newContent);
										newContent.setObjId(Cli_Object_Key);
								
										newContent.setIntValue(Cli_Object_Value);
							
										writeToServerPipes.get(writeServerId).writeObject(newMessage);

										System.out.println("\n [debug]writeServerId = " + writeServerId);
										System.out.println("\n [debug]Client send msg: " + " MsgType=" + newMessage.getMessageType() + " ObjId=" + newMessage.getContentObject().getObjId() + " ObjValue=" + newMessage.getContentObject().getIntValue());
								
										try {
												RecvMessage = (MessageObject)readFromServerPipes.get(writeServerId).readObject();
											} catch (ClassNotFoundException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}	
								
										System.out.println("\n [debug]Client recv msg: " + " MsgType=" + RecvMessage.getMessageType());

									}
									catch (IOException e) 
									{ 
										e.printStackTrace();  
									}  
								}
							}
							else
							{
								System.out.println("[debug]Undefined Cli_Operation_Type");
							}
						}
					}
				}catch (IOException e) 
				{ 
					e.printStackTrace();  
				}  
			}
			

		}		
	}
