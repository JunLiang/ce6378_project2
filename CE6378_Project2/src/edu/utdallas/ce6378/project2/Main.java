package edu.utdallas.ce6378.project2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;



public class Main {
	
	private static HashMap<Integer, NodeConfiguration> allServerNodes = new HashMap<Integer, NodeConfiguration> ();
	private static HashMap<Integer, NodeConfiguration> allClientNodes = new HashMap<Integer, NodeConfiguration> ();	
		
	public static HashMap<Integer, NodeConfiguration> getAllServerNodes() {
		return Main.allServerNodes;
	}

	public static void setAllServerNodes(HashMap<Integer, NodeConfiguration> allNodes) {
		Main.allServerNodes = allNodes;
	}
	
	public static HashMap<Integer, NodeConfiguration> getAllClientNodes() {
		return allClientNodes;
	}

	public static void setAllClientNodes(
			HashMap<Integer, NodeConfiguration> allClientNodes) {
		Main.allClientNodes = allClientNodes;
	}

	public static void main(String[] args) {
		/*TODO: create a configuration file with node id, host, port mapping*/
		/*Start the host with node id as configuration*/
		
		//Read Node configuration files 
		//Creating  current node based node id assigned to this process
		
		Integer localNodeId = Integer.valueOf(args[0]);
		Integer localNodeMode = 0;
		
		NodeConfiguration localNodeConfig = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader ("all_nodes.cfg"));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(",");
				if (fields.length == 4) {
					Integer nodeId = Integer.valueOf(fields[0].trim());
					String host = fields[1].trim();
					Integer port = Integer.valueOf(fields[2].trim());
					Integer nodeMode = Integer.valueOf(fields[3].trim());
					
					NodeConfiguration nodeConfig = new NodeConfiguration(host, port, nodeId);
					//Server Node Model
					if (nodeMode == 0) {
						Main.getAllServerNodes().put(nodeId,nodeConfig);
					} else {
						Main.getAllClientNodes().put(nodeId,nodeConfig);
					}
					
					//Add node configuration to the global storage
										
					if (nodeId.equals(localNodeId)) {
						localNodeConfig = nodeConfig;
						localNodeMode = nodeMode;
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		
		System.out.println("Node Id :" + localNodeId + " read " + Main.getAllServerNodes().size() + " server nodes " + Main.getAllClientNodes().size() + " client nodes.");

		if (localNodeConfig != null || localNodeId.equals(-1)) {
			
			if (localNodeId.equals(-1)) {
				ServerControlUtil serverControl = new ServerControlUtil();
				serverControl.simulate();
				
			}else { 			
				if (localNodeMode.equals(0)) {
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
				} else {
					ContentClient localNode;
					localNode = new ContentClient(localNodeConfig);
					localNode.simulate();
				}
			}
		}
	}
}
