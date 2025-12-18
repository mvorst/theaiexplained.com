package com.thebridgetoai.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebridgetoai.website.dao.DataStoreDao;
import com.thebridgetoai.website.model.DataStore;
import com.thebridgetoai.website.model.DataStoreList;
import com.thebridgetoai.website.model.DataStoreRequest;
import com.thebridgetoai.website.model.DataStoreResponse;

@ExtendWith(MockitoExtension.class)
class DataStoreServiceTest {
	
	@Mock
	private DataStoreDao dataStoreDao;

	@Mock
	private ObjectMapper objectMapper;
	
	@Mock
	private SecurityContext securityContext;
	
	@Mock
	private Authentication authentication;
	
	@InjectMocks
	private DataStoreService dataStoreService;
	
	private UUID applicationUuid;
	private String namespace;
	private String id;
	private DataStoreRequest request;
	private Map<String, Object> testData;
	
	@BeforeEach
	void setUp() {
		applicationUuid = UUID.randomUUID();
		namespace = "test-namespace";
		id = "test-id";
		
		testData = new HashMap<>();
		testData.put("key1", "value1");
		testData.put("key2", 123);
		
		request = new DataStoreRequest();
		request.setData(testData);
		
		// Set up security context
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn("testuser");
	}
	
	@Test
	void testCreateValue() throws Exception {
		// Arrange
		DataStore savedItem = new DataStore();
		savedItem.setApplicationUuid(applicationUuid);
		savedItem.setNamespace(namespace);
		savedItem.setId(id);
		savedItem.setDataValue("{\"key1\":\"value1\",\"key2\":123}");
		savedItem.setCreatedDate(new Date());
		savedItem.setCreatedBySubject("testuser");
		savedItem.setUpdatedDate(new Date());
		savedItem.setUpdatedBySubject("testuser");
		
		when(objectMapper.writeValueAsString(testData)).thenReturn("{\"key1\":\"value1\",\"key2\":123}");
		when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testData);
		when(dataStoreDao.saveDataStore(any(DataStore.class))).thenReturn(CompletableFuture.completedFuture(savedItem));
		
		// Act
		CompletableFuture<DataStoreResponse> result = dataStoreService.createValue(applicationUuid, namespace, id, request);
		DataStoreResponse response = result.get();
		
		// Assert
		assertNotNull(response);
		assertEquals(applicationUuid, response.getApplicationUuid());
		assertEquals(namespace, response.getNamespace());
		assertEquals(id, response.getId());
		assertEquals(testData, response.getData());
		assertEquals("testuser", response.getCreatedBySubject());
		assertEquals("testuser", response.getUpdatedBySubject());
		
		verify(dataStoreDao, times(1)).saveDataStore(any(DataStore.class));
		verify(objectMapper, times(1)).writeValueAsString(testData);
	}
	
	@Test
	void testUpdateValue() throws Exception {
		// Arrange
		DataStore existingItem = new DataStore();
		existingItem.setApplicationUuid(applicationUuid);
		existingItem.setNamespace(namespace);
		existingItem.setId(id);
		existingItem.setDataValue("{\"old\":\"data\"}");
		existingItem.setCreatedDate(new Date());
		existingItem.setCreatedBySubject("creator");
		existingItem.setUpdatedDate(new Date());
		existingItem.setUpdatedBySubject("creator");
		
		when(dataStoreDao.getDataStore(applicationUuid, namespace, id)).thenReturn(CompletableFuture.completedFuture(existingItem));
		when(objectMapper.writeValueAsString(testData)).thenReturn("{\"key1\":\"value1\",\"key2\":123}");
		when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testData);
		when(dataStoreDao.updateDataStore(any(DataStore.class))).thenReturn(CompletableFuture.completedFuture(existingItem));
		
		// Act
		CompletableFuture<DataStoreResponse> result = dataStoreService.updateValue(applicationUuid, namespace, id, request);
		DataStoreResponse response = result.get();
		
		// Assert
		assertNotNull(response);
		assertEquals(applicationUuid, response.getApplicationUuid());
		assertEquals(namespace, response.getNamespace());
		assertEquals(id, response.getId());
		assertEquals(testData, response.getData());
		assertEquals("creator", response.getCreatedBySubject());
		assertEquals("testuser", response.getUpdatedBySubject());
		
		verify(dataStoreDao, times(1)).getDataStore(applicationUuid, namespace, id);
		verify(dataStoreDao, times(1)).updateDataStore(any(DataStore.class));
		verify(objectMapper, times(1)).writeValueAsString(testData);
	}
	
	@Test
	void testUpdateValueNotFound() {
		// Arrange
		when(dataStoreDao.getDataStore(applicationUuid, namespace, id)).thenReturn(CompletableFuture.completedFuture(null));
		
		// Act & Assert
		CompletableFuture<DataStoreResponse> result = dataStoreService.updateValue(applicationUuid, namespace, id, request);
		
		assertThrows(Exception.class, () -> result.get());
		verify(dataStoreDao, times(1)).getDataStore(applicationUuid, namespace, id);
		verify(dataStoreDao, never()).updateDataStore(any(DataStore.class));
	}
	
	@Test
	void testGetValue() throws Exception {
		// Arrange
		DataStore item = new DataStore();
		item.setApplicationUuid(applicationUuid);
		item.setNamespace(namespace);
		item.setId(id);
		item.setDataValue("{\"key1\":\"value1\",\"key2\":123}");
		item.setCreatedDate(new Date());
		item.setCreatedBySubject("testuser");
		item.setUpdatedDate(new Date());
		item.setUpdatedBySubject("testuser");
		
		when(dataStoreDao.getDataStore(applicationUuid, namespace, id)).thenReturn(CompletableFuture.completedFuture(item));
		when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testData);
		
		// Act
		CompletableFuture<DataStoreResponse> result = dataStoreService.getDataStore(applicationUuid, namespace, id);
		DataStoreResponse response = result.get();
		
		// Assert
		assertNotNull(response);
		assertEquals(applicationUuid, response.getApplicationUuid());
		assertEquals(namespace, response.getNamespace());
		assertEquals(id, response.getId());
		assertEquals(testData, response.getData());
		
		verify(dataStoreDao, times(1)).getDataStore(applicationUuid, namespace, id);
	}
	
	@Test
	void testGetValueNotFound() throws Exception {
		// Arrange
		when(dataStoreDao.getDataStore(applicationUuid, namespace, id)).thenReturn(CompletableFuture.completedFuture(null));
		
		// Act
		CompletableFuture<DataStoreResponse> result = dataStoreService.getDataStore(applicationUuid, namespace, id);
		DataStoreResponse response = result.get();
		
		// Assert
		assertNull(response);
		verify(dataStoreDao, times(1)).getDataStore(applicationUuid, namespace, id);
	}
	
	@Test
	void testDeleteValue() throws Exception {
		// Arrange
		when(dataStoreDao.deleteDataStore(applicationUuid, namespace, id)).thenReturn(CompletableFuture.completedFuture(null));
		
		// Act
		CompletableFuture<Void> result = dataStoreService.deleteValue(applicationUuid, namespace, id);
		result.get();
		
		// Assert
		verify(dataStoreDao, times(1)).deleteDataStore(applicationUuid, namespace, id);
	}
	
	@Test
	void testCreateListItem() throws Exception {
		// Arrange
		String sortKey = "2024-01-15T10:30:00Z";
		DataStoreList savedItem = new DataStoreList();
		savedItem.setApplicationUuid(applicationUuid);
		savedItem.setNamespace(namespace);
		savedItem.setId(id);
		savedItem.setSortKey(sortKey);
		savedItem.setDataValue("{\"key1\":\"value1\",\"key2\":123}");
		savedItem.setCreatedDate(new Date());
		savedItem.setCreatedBySubject("testuser");
		savedItem.setUpdatedDate(new Date());
		savedItem.setUpdatedBySubject("testuser");
		
		when(objectMapper.writeValueAsString(testData)).thenReturn("{\"key1\":\"value1\",\"key2\":123}");
		when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testData);

		// Act
		CompletableFuture<DataStoreResponse> result = dataStoreService.createListItem(applicationUuid, namespace, id, sortKey, request);
		DataStoreResponse response = result.get();
		
		// Assert
		assertNotNull(response);
		assertEquals(applicationUuid, response.getApplicationUuid());
		assertEquals(namespace, response.getNamespace());
		assertEquals(id, response.getId());
		assertEquals(sortKey, response.getSortKey());
		assertEquals(testData, response.getData());
		assertEquals("testuser", response.getCreatedBySubject());
		assertEquals("testuser", response.getUpdatedBySubject());
		
		verify(objectMapper, times(1)).writeValueAsString(testData);
	}
	
	@Test
	void testWithClassName() throws Exception {
		// Arrange
		request.setClassName("com.example.MyClass");
		DataStore savedItem = new DataStore();
		savedItem.setApplicationUuid(applicationUuid);
		savedItem.setNamespace(namespace);
		savedItem.setId(id);
		savedItem.setDataValue("{\"key1\":\"value1\",\"key2\":123}");
		savedItem.setClassName("com.example.MyClass");
		savedItem.setCreatedDate(new Date());
		savedItem.setCreatedBySubject("testuser");
		savedItem.setUpdatedDate(new Date());
		savedItem.setUpdatedBySubject("testuser");
		
		when(objectMapper.writeValueAsString(testData)).thenReturn("{\"key1\":\"value1\",\"key2\":123}");
		when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testData);
		when(dataStoreDao.saveDataStore(any(DataStore.class))).thenReturn(CompletableFuture.completedFuture(savedItem));
		
		// Act
		CompletableFuture<DataStoreResponse> result = dataStoreService.createValue(applicationUuid, namespace, id, request);
		DataStoreResponse response = result.get();
		
		// Assert
		assertNotNull(response);
		verify(dataStoreDao, times(1)).saveDataStore(argThat(item ->
			"com.example.MyClass".equals(item.getClassName())
		));
	}
	
	@Test
	void testAnonymousUser() throws Exception {
		// Arrange
		when(authentication.isAuthenticated()).thenReturn(false);
		
		DataStore savedItem = new DataStore();
		savedItem.setApplicationUuid(applicationUuid);
		savedItem.setNamespace(namespace);
		savedItem.setId(id);
		savedItem.setDataValue("{\"key1\":\"value1\",\"key2\":123}");
		savedItem.setCreatedDate(new Date());
		savedItem.setCreatedBySubject("anonymous");
		savedItem.setUpdatedDate(new Date());
		savedItem.setUpdatedBySubject("anonymous");
		
		when(objectMapper.writeValueAsString(testData)).thenReturn("{\"key1\":\"value1\",\"key2\":123}");
		when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testData);
		when(dataStoreDao.saveDataStore(any(DataStore.class))).thenReturn(CompletableFuture.completedFuture(savedItem));
		
		// Act
		CompletableFuture<DataStoreResponse> result = dataStoreService.createValue(applicationUuid, namespace, id, request);
		DataStoreResponse response = result.get();
		
		// Assert
		assertNotNull(response);
		assertEquals("anonymous", response.getCreatedBySubject());
		assertEquals("anonymous", response.getUpdatedBySubject());
		
		verify(dataStoreDao, times(1)).saveDataStore(argThat(item ->
			"anonymous".equals(item.getCreatedBySubject()) && "anonymous".equals(item.getUpdatedBySubject())
		));
	}
}