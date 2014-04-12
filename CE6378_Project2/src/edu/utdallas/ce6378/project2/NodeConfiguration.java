package edu.utdallas.ce6378.project2;

public class NodeConfiguration {
	private Integer portNo;
	private String  hostName;	
	private Integer nodeId;
	
	public NodeConfiguration (String hostName, Integer portNo, Integer nodeId) {
		this.hostName = hostName;
		this.portNo = portNo;
		this.nodeId = nodeId;
	}

	public Integer getPortNo() {
		return portNo;
	}

	public void setPortNo(Integer portNo) {
		this.portNo = portNo;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

}
