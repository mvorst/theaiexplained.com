package com.thebridgetoai.website.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataStoreListResponse {
	private List<DataStoreResponse> list;
	private String cursor; // Encoded pagination cursor
	
	public List<DataStoreResponse> getList() {
		return list;
	}
	
	public void setList(List<DataStoreResponse> list) {
		this.list = list;
	}
	
	public String getCursor() {
		return cursor;
	}
	
	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
	
	@JsonIgnore
	public boolean hasCursor() {
		return cursor != null && !cursor.isEmpty();
	}
}