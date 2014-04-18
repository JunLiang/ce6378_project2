package edu.utdallas.ce6378.project2;

import java.io.Serializable;

public class MessageObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8421848454689383113L;
	private MessageType messageType;	
	private ContentObject contentObject;
	private Integer fromServerId;
	private VectorTimestamp timestamp;
			
	public  MessageObject () {
		fromServerId = -1;
		contentObject = null;
		messageType = MessageType.SERVER_UNAVAILABLE;
		timestamp = new VectorTimestamp (Constant.numberOfServers);
	}

	public VectorTimestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(VectorTimestamp timestamp) {
		this.timestamp.setTimeVector(timestamp.getTimeVector());
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType requestType) {
		this.messageType = requestType;
	}

	public ContentObject getContentObject() {
		return contentObject;
	}

	public void setContentObject(ContentObject requstObject) {
		this.contentObject = requstObject;
	}

	public Integer getFromServerId() {
		return fromServerId;
	}

	public void setFromServerId(Integer fromServerId) {
		this.fromServerId = fromServerId;
	}
	
	public String printMessageObject() {
		StringBuilder a = new StringBuilder();
		
		a.append("Message[").append(this.getMessageType()).append(", ")
		.append(this.getFromServerId()).append(",").append(this.getContentObject() == null ? null : this.getContentObject().printContentObject())
		.append(", ").append(this.getTimestamp().printTimestamp()).append("]");
		
		return a.toString();
	}

}
