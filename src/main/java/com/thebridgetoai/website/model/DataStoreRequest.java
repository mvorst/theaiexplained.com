package com.thebridgetoai.website.model;

import java.util.Map;

public class DataStoreRequest {
	private Map<String, Object> data;
	private String className;
	
	public Map<String, Object> getData() {
		return data;
	}
	
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
}