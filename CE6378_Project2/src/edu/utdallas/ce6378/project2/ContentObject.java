package edu.utdallas.ce6378.project2;

import java.io.Serializable;

public class ContentObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 254381135253496165L;


	private Integer objId; //Key 
	
	private String strValue;
	
	private Integer intValue;
	
	private VectorTimestamp timestamp;
	
	public ContentObject(){
		objId = -1;
		strValue = "";
		intValue = 0;
		timestamp = new VectorTimestamp (3);
	}

	public Integer getObjId() {
		return objId;
	}


	public void setObjId(Integer objId) {
		this.objId = objId;
	}


	public String getStrValue() {
		return strValue;
	}


	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}


	public Integer getIntValue() {
		return intValue;
	}


	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public VectorTimestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(VectorTimestamp timestamp) {
		if (this.timestamp.getTimeVector().length == timestamp.getTimeVector().length) {
			//Copy the time vector 
			this.timestamp.setTimeVector(timestamp.getTimeVector()) ;
		}
	}
	
	public String printContentObject() {
		StringBuilder a = new StringBuilder();
		a.append("ContentObject [" ).append (this.getObjId()) .append( ", " )
		.append( this.getIntValue() ).append (", " ).append(this.getStrValue() )
		.append( ", " ).append( this.getTimestamp().printTimestamp()).append("]");
		return a.toString();
	}

}
