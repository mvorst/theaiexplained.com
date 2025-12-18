package com.thebridgetoai.website.model;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DataStoreResponse {
	private UUID applicationUuid;
	private String namespace;
	private String id;
	private String sortKey; // Only for list items
	private Map<String, Object> data;
	private Date createdDate;
	private String createdBySubject;
	private Date updatedDate;
	private String updatedBySubject;
	
	public UUID getApplicationUuid() {
		return applicationUuid;
	}
	
	public void setApplicationUuid(UUID applicationUuid) {
		this.applicationUuid = applicationUuid;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSortKey() {
		return sortKey;
	}
	
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}
	
	public Map<String, Object> getData() {
		return data;
	}
	
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public String getCreatedBySubject() {
		return createdBySubject;
	}
	
	public void setCreatedBySubject(String createdBySubject) {
		this.createdBySubject = createdBySubject;
	}
	
	public Date getUpdatedDate() {
		return updatedDate;
	}
	
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	public String getUpdatedBySubject() {
		return updatedBySubject;
	}
	
	public void setUpdatedBySubject(String updatedBySubject) {
		this.updatedBySubject = updatedBySubject;
	}
}