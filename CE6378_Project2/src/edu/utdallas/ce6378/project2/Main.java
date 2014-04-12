package edu.utdallas.ce6378.project2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;



public class Main {
	
	private static ArrayList<NodeConfiguration> allNodes = new ArrayList<NodeConfiguration> ();			
		
	public static ArrayList<NodeConfiguration> getAllNodes() {
		return Main.allNodes;
	}

	public static void setAllNodes(ArrayList<NodeConfiguration> allNodes) {
		Main.allNodes = allNodes;
	}
	
	public static void main(String[] args) {
		/*TODO: create a configuration file with node id, host, port mapping*/
		/*Start the host with node id as configuration*/
		
		//Read Node configuration files 
		//Creating  current node based node id assigned to this process
		
		Integer localNodeId = Integer.valueOf(args[0]);
		NodeConfiguration localNodeConfig = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader ("all_nodes.cfg"));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(",");
				if (fields.length == 3) {
					Integer nodeId = Integer.valueOf(fields[0].trim());
					String host = fields[1].trim();
					Integer port = Integer.valueOf(fields[2].trim());
					NodeConfiguration nodeConfig = new NodeConfiguration(host, port, nodeId);
					
					//Add node configuration to the global storage
					Main.getAllNodes().add(nodeConfig);
					
					if (nodeId == localNodeId) {
						localNodeConfig = nodeConfig;
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Node Id :" + localNodeId + " read " + Main.getAllNodes().size() + " nodes ");
		if (localNodeConfig != null) {
			ContentServer localNode;
			try {
				localNode = new ContentServer(localNodeConfig);
				localNode.start();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
