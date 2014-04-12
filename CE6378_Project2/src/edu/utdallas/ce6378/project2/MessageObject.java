package edu.utdallas.ce6378.project2;

import java.io.Serializable;

public class MessageObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8421848454689383113L;
	private MessageType requestType;	
	private ContentObject requstObject;
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

	public MessageType getRequestType() {
		return requestType;
	}

	public void setRequestType(MessageType requestType) {
		this.requestType = requestType;
	}

	public ContentObject getRequstObject() {
		return requstObject;
	}

	public void setRequstObject(ContentObject requstObject) {
		this.requstObject = requstObject;
	}

	public Integer getFromServerId() {
		return fromServerId;
	}

	public void setFromServerId(Integer fromServerId) {
		this.fromServerId = fromServerId;
	}

}
