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

}
