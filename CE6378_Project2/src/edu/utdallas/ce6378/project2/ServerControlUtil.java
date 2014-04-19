package edu.utdallas.ce6378.project2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class ServerControlUtil {
	
	HashMap<Integer, Socket> serverSockets ;
	HashMap<Integer, ObjectOutputStream> writeToServerPipes;
	HashMap<Integer, ObjectInputStream> readFromServerPipes;
	
	
	public ServerControlUtil() {
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
		
		Scanner consoleIn = null;
		
		try {
			Thread.sleep(6000);

			establishConnectionToServers();
			
			consoleIn = new Scanner (System.in);
			
			while (true) {
				try {
					String input = consoleIn.nextLine();
					
					String[] params = input.split(",");
					
					String command = params[0].trim();
					
					if (command.equalsIgnoreCase("ISOLATE")) {
						
						//Command ISOLATE is used to partition the servers into 
						//two groups. The parameters are the nodes in 1 group
						//and the rest are in the second group.
						assert (params.length > 2);
						
						HashSet<Integer> set1 = new HashSet<Integer>();
						
						for (int i = 0; i < 7 ; i++) {
							set1.add(i);
						}
						
						HashSet<Integer> set2 = new HashSet<Integer>();
						
						for (int i = 1; i < params.length; i++) {
							Integer serverId = Integer.valueOf(params[i].trim());
							if (serverId < 7 && serverId >= 0) {
								set1.remove(serverId);
								set2.add(serverId);
							}
						}
						
						for (Integer serverId1 : set1){
							for (Integer serverId2 : set2) {
								
								System.out.println("Server 1: "+ serverId1 + " Server 2: "+serverId2);
								
								MessageObject newMessage1 = new MessageObject();
								newMessage1.setFromServerId(serverId1);
								newMessage1.setMessageType(MessageType.SERVER_CONTROL_ISOLATE);
								
								this.writeToServerPipes.get(serverId2).writeObject(newMessage1);
								MessageObject returnMessage1 = (MessageObject)this.readFromServerPipes.get(serverId2).readObject();
								
								System.out.println(returnMessage1.printMessageObject());
		
								MessageObject newMessage2 = new MessageObject();
								newMessage2.setFromServerId(serverId2);
								newMessage2.setMessageType(MessageType.SERVER_CONTROL_ISOLATE);
								
								this.writeToServerPipes.get(serverId1).writeObject(newMessage1);
								MessageObject returnMessage2 = (MessageObject)this.readFromServerPipes.get(serverId1).readObject();
								
								System.out.println(returnMessage2.printMessageObject());
							}
						}
												
					} else if (command.equalsIgnoreCase("FAIL")) {
						assert(params.length == 2) ;
						Integer serverId1 = Integer.valueOf(params[1].trim());
						
						MessageObject newMessage1 = new MessageObject();
						newMessage1.setFromServerId(-1);
						newMessage1.setMessageType(MessageType.SERVER_CONTROL_FAIL);
						
						this.writeToServerPipes.get(serverId1).writeObject(newMessage1);
						MessageObject returnMessage1 = (MessageObject)this.readFromServerPipes.get(serverId1).readObject();
						
						System.out.println(returnMessage1.printMessageObject());
						
					} 
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
			
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
			for (ObjectOutputStream out : this.writeToServerPipes.values()) {
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
			}
			
			if (consoleIn != null) {
				consoleIn.close();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
