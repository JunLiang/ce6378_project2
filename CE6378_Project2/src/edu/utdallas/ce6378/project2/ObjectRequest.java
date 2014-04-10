package edu.utdallas.ce6378.project2;

public class ObjectRequest {
	
	private RequestType requestType;	
	private ContentObject requstObject;
	private VectorTimestamp requestTimestamp;
			
	public  ObjectRequest () {
				
	}

	public VectorTimestamp getRequestTimestamp() {
		return requestTimestamp;
	}

	public void setRequestTimestamp(VectorTimestamp requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
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

}
