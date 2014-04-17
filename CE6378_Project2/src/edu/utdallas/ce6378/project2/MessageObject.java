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
			
	public  MessageObject () {
		fromServerId = -1;
		contentObject = null;
		messageType = MessageType.SERVER_UNAVAILABLE;
	}

	public VectorTimestamp getTimestamp() {
		return contentObject != null ? contentObject.getTimestamp() : null;
	}

	public void setTimestamp(VectorTimestamp timestamp) {
		if (contentObject != null ) {
			contentObject.setTimestamp(timestamp);
		}
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
