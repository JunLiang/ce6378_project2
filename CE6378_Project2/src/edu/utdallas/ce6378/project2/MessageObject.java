package edu.utdallas.ce6378.project2;

import java.io.Serializable;

public class MessageObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8421848454689383113L;
	private MessageType messageType;	
	private ContentObject contentObject;
	private VectorTimestamp timestamp;
	private Integer fromServerId;
			
	public  MessageObject () {
		fromServerId = -1;
	}

	public VectorTimestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(VectorTimestamp requestTimestamp) {
		this.timestamp = requestTimestamp;
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

}
