package edu.utdallas.ce6378.project2;

import java.io.Serializable;

public class RequestObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8421848454689383113L;
	private RequestType requestType;	
	private ContentObject requstObject;
	private VectorTimestamp timestamp;
	private Integer fromServerId;
			
	public  RequestObject () {
		fromServerId = -1;
	}

	public VectorTimestamp getRequestTimestamp() {
		return timestamp;
	}

	public void setRequestTimestamp(VectorTimestamp requestTimestamp) {
		this.timestamp = requestTimestamp;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
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
