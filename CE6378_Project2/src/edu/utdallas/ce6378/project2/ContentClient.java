package edu.utdallas.ce6378.project2;

//my changes
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ContentClient {
	
	HashMap<Integer, Socket> serverSockets ;
	HashMap<Integer, ObjectOutputStream> writeToServerPipes;
	HashMap<Integer, ObjectInputStream> readFromServerPipes;
	
	Integer nodeId; //client node Id;
	
	public ContentClient (NodeConfiguration localNodeConfig) {		
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
		}
		
		
		
	}
	
	class clientThread implements Runnable {
		
		public clientThread() {
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
