package com.thebridgetoai.website.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattvorst.shared.util.CursorUtils;
import com.thebridgetoai.website.dao.DataStoreDao;
import com.thebridgetoai.website.model.DataStore;
import com.thebridgetoai.website.model.DataStoreList;
import com.thebridgetoai.website.model.DataStoreListResponse;
import com.thebridgetoai.website.model.DataStoreRequest;
import com.thebridgetoai.website.model.DataStoreResponse;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class DataStoreService {
	
	@Autowired
	private DataStoreDao dataStoreDao;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private String getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			return authentication.getName();
		}
		return "anonymous";
	}
	
	// Simple Key-Value Operations
	
	public CompletableFuture<DataStoreResponse> createValue(UUID applicationUuid, String namespace, String id, DataStoreRequest request) {
		DataStore item = new DataStore();
		item.setApplicationUuid(applicationUuid);
		item.setNamespace(namespace);
		item.setId(id);
		
		// Serialize data to JSON string
		try {
			item.setDataValue(objectMapper.writeValueAsString(request.getData()));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize data", e);
		}
		
		if (request.getClassName() != null) {
			item.setClassName(request.getClassName());
		}
		
		// Set audit fields
		Date now = new Date();
		String user = getCurrentUser();
		item.setCreatedDate(now);
		item.setCreatedBySubject(user);
		item.setUpdatedDate(now);
		item.setUpdatedBySubject(user);
		
		return dataStoreDao.saveDataStore(item)
				.thenApply(this::convertToResponse);
	}
	
	public CompletableFuture<DataStoreResponse> updateValue(UUID applicationUuid, String namespace, String id, DataStoreRequest request) {
		return dataStoreDao.getDataStore(applicationUuid, namespace, id)
				.thenCompose(existing -> {
					if (existing == null) {
						throw new RuntimeException("Item not found");
					}
					
					// Update data
					try {
						existing.setDataValue(objectMapper.writeValueAsString(request.getData()));
					} catch (JsonProcessingException e) {
						throw new RuntimeException("Failed to serialize data", e);
					}
					
					if (request.getClassName() != null) {
						existing.setClassName(request.getClassName());
					}
					
					// Update audit fields
					existing.setUpdatedDate(new Date());
					existing.setUpdatedBySubject(getCurrentUser());
					
					return dataStoreDao.updateDataStore(existing);
				})
				.thenApply(this::convertToResponse);
	}
	
	public CompletableFuture<DataStoreResponse> getDataStore(UUID applicationUuid, String namespace, String id) {
		return dataStoreDao.getDataStore(applicationUuid, namespace, id)
				.thenApply(item -> {
					if (item == null) {
						return null;
					}
					return convertToResponse(item);
				});
	}
	
	public CompletableFuture<Void> deleteValue(UUID applicationUuid, String namespace, String id) {
		return dataStoreDao.deleteDataStore(applicationUuid, namespace, id);
	}
	
	// List Operations
	
	public CompletableFuture<DataStoreResponse> createListItem(UUID applicationUuid, String namespace, String id, String sortKey, DataStoreRequest request) {
		DataStoreList item = new DataStoreList();
		item.setApplicationUuid(applicationUuid);
		item.setNamespace(namespace);
		item.setId(id);
		item.setSortKey(sortKey);
		
		// Serialize data to JSON string
		try {
			item.setDataValue(objectMapper.writeValueAsString(request.getData()));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize data", e);
		}
		
		if (request.getClassName() != null) {
			item.setClassName(request.getClassName());
		}
		
		// Set audit fields
		Date now = new Date();
		String user = getCurrentUser();
		item.setCreatedDate(now);
		item.setCreatedBySubject(user);
		item.setUpdatedDate(now);
		item.setUpdatedBySubject(user);
		
		return dataStoreDao.saveDataStoreListItem(item)
				.thenApply(this::convertListToResponse);
	}
	
	public CompletableFuture<DataStoreResponse> updateListItem(UUID applicationUuid, String namespace, String id, String sortKey, DataStoreRequest request) {
		return dataStoreDao.getDataStoreListItem(applicationUuid, namespace, id, sortKey)
				.thenCompose(existing -> {
					if (existing == null) {
						throw new RuntimeException("Item not found");
					}
					
					// Update data
					try {
						existing.setDataValue(objectMapper.writeValueAsString(request.getData()));
					} catch (JsonProcessingException e) {
						throw new RuntimeException("Failed to serialize data", e);
					}
					
					if (request.getClassName() != null) {
						existing.setClassName(request.getClassName());
					}
					
					// Update audit fields
					existing.setUpdatedDate(new Date());
					existing.setUpdatedBySubject(getCurrentUser());
					
					return dataStoreDao.updateDataStoreListItem(existing);
				})
				.thenApply(this::convertListToResponse);
	}
	
	public CompletableFuture<DataStoreResponse> getListItem(UUID applicationUuid, String namespace, String id, String sortKey) {
		return dataStoreDao.getDataStoreListItem(applicationUuid, namespace, id, sortKey)
				.thenApply(item -> {
					if (item == null) {
						return null;
					}
					return convertListToResponse(item);
				});
	}
	
	public CompletableFuture<Void> deleteListItem(UUID applicationUuid, String namespace, String id, String sortKey) {
		return dataStoreDao.deleteDataStoreListItem(applicationUuid, namespace, id, sortKey);
	}
	
	public CompletableFuture<DataStoreListResponse> listItems(UUID applicationUuid, String namespace, String id,
			String startKey, String endKey, Integer limit, String sortOrder, String cursor) {
		
		boolean ascending = !"DESC".equalsIgnoreCase(sortOrder);
		Map<String, AttributeValue> lastEvaluatedKey = null;
		
		if (cursor != null && !cursor.isEmpty()) {
			lastEvaluatedKey = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);
		}
		
		if (limit == null) {
			limit = 100;
		} else if (limit > 1000) {
			limit = 1000;
		}
		
		return dataStoreDao.getDataStoreListItems(applicationUuid, namespace, id, startKey, endKey, limit, ascending, lastEvaluatedKey)
				.thenApply(result -> {
					DataStoreListResponse response = new DataStoreListResponse();
					
					List<DataStoreResponse> items = result.getList().stream()
							.map(this::convertListToResponse)
							.collect(Collectors.toList());
					
					response.setList(items);
					
					if (result.hasCursor()) {
						response.setCursor(result.getCursor());
					}
					
					return response;
				});
	}
	
	public CompletableFuture<Void> deleteAllListItems(UUID applicationUuid, String namespace, String id) {
		return dataStoreDao.deleteDataStoreListItems(applicationUuid, namespace, id);
	}
	
	// Helper methods
	
	private DataStoreResponse convertToResponse(DataStore item) {
		DataStoreResponse response = new DataStoreResponse();
		response.setApplicationUuid(item.getApplicationUuid());
		response.setNamespace(item.getNamespace());
		response.setId(item.getId());
		
		// Deserialize JSON data
		try {
			if (item.getDataValue() != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = objectMapper.readValue(item.getDataValue(), Map.class);
				response.setData(data);
			}
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to deserialize data", e);
		}
		
		response.setCreatedDate(item.getCreatedDate());
		response.setCreatedBySubject(item.getCreatedBySubject());
		response.setUpdatedDate(item.getUpdatedDate());
		response.setUpdatedBySubject(item.getUpdatedBySubject());
		
		return response;
	}
	
	private DataStoreResponse convertListToResponse(DataStoreList item) {
		DataStoreResponse response = new DataStoreResponse();
		response.setApplicationUuid(item.getApplicationUuid());
		response.setNamespace(item.getNamespace());
		response.setId(item.getId());
		response.setSortKey(item.getSortKey());
		
		// Deserialize JSON data
		try {
			if (item.getDataValue() != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = objectMapper.readValue(item.getDataValue(), Map.class);
				response.setData(data);
			}
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to deserialize data", e);
		}
		
		response.setCreatedDate(item.getCreatedDate());
		response.setCreatedBySubject(item.getCreatedBySubject());
		response.setUpdatedDate(item.getUpdatedDate());
		response.setUpdatedBySubject(item.getUpdatedBySubject());
		
		return response;
	}
}