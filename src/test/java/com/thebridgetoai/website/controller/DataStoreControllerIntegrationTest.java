package com.thebridgetoai.website.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebridgetoai.website.model.DataStoreRequest;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"spring.profiles.active=test",
	"aws.dynamodb.endpoint=http://localhost:8000"  // Use DynamoDB Local for testing
})
class DataStoreControllerIntegrationTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private UUID applicationUuid;
	private String namespace;
	private String id;
	private DataStoreRequest request;
	
	@BeforeEach
	void setUp() {
		applicationUuid = UUID.randomUUID();
		namespace = "test-namespace";
		id = "test-id-" + System.currentTimeMillis(); // Make unique for each test
		
		Map<String, Object> testData = new HashMap<>();
		testData.put("key1", "value1");
		testData.put("key2", 123);
		testData.put("nested", Map.of("subkey", "subvalue"));
		
		request = new DataStoreRequest();
		request.setData(testData);
	}
	
	@Test
	void testCreateAndGetValue() throws Exception {
		// Create a value
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/", 
				applicationUuid, namespace, id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.applicationUuid").value(applicationUuid.toString()))
				.andExpect(jsonPath("$.namespace").value(namespace))
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.data.key1").value("value1"))
				.andExpect(jsonPath("$.data.key2").value(123))
				.andExpect(jsonPath("$.data.nested.subkey").value("subvalue"));
		
		// Get the value
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}", 
				applicationUuid, namespace, id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.applicationUuid").value(applicationUuid.toString()))
				.andExpect(jsonPath("$.namespace").value(namespace))
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.data.key1").value("value1"))
				.andExpect(jsonPath("$.data.key2").value(123));
	}
	
	@Test
	void testUpdateValue() throws Exception {
		// First create a value
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/", 
				applicationUuid, namespace, id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());
		
		// Update the value
		Map<String, Object> updatedData = new HashMap<>();
		updatedData.put("key1", "updated-value");
		updatedData.put("key3", "new-key");
		
		DataStoreRequest updateRequest = new DataStoreRequest();
		updateRequest.setData(updatedData);
		
		mockMvc.perform(put("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}", 
				applicationUuid, namespace, id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.key1").value("updated-value"))
				.andExpect(jsonPath("$.data.key3").value("new-key"))
				.andExpect(jsonPath("$.data.key2").doesNotExist());
	}
	
	@Test
	void testDeleteValue() throws Exception {
		// First create a value
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/", 
				applicationUuid, namespace, id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());
		
		// Delete the value
		mockMvc.perform(delete("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}", 
				applicationUuid, namespace, id))
				.andExpect(status().isNoContent());
		
		// Verify it's deleted
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}", 
				applicationUuid, namespace, id))
				.andExpect(status().isNotFound());
	}
	
	@Test
	void testCreateDuplicate() throws Exception {
		// Create a value
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/", 
				applicationUuid, namespace, id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());
		
		// Try to create the same value again
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/", 
				applicationUuid, namespace, id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("Key already exists"));
	}
	
	@Test
	void testUpdateNonExistent() throws Exception {
		mockMvc.perform(put("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}", 
				applicationUuid, namespace, "non-existent-id")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Key not found"));
	}
	
	@Test
	void testListOperations() throws Exception {
		String sortKey1 = "2024-01-15T10:00:00Z";
		String sortKey2 = "2024-01-15T11:00:00Z";
		String sortKey3 = "2024-01-15T12:00:00Z";
		
		// Create multiple list items
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}/", 
				applicationUuid, namespace, id, sortKey1)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());
		
		Map<String, Object> data2 = Map.of("item", "2");
		DataStoreRequest request2 = new DataStoreRequest();
		request2.setData(data2);
		
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}/", 
				applicationUuid, namespace, id, sortKey2)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request2)))
				.andExpect(status().isCreated());
		
		Map<String, Object> data3 = Map.of("item", "3");
		DataStoreRequest request3 = new DataStoreRequest();
		request3.setData(data3);
		
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}/", 
				applicationUuid, namespace, id, sortKey3)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request3)))
				.andExpect(status().isCreated());
		
		// List items in ascending order
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list/", 
				applicationUuid, namespace, id)
				.param("sortOrder", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list").isArray())
				.andExpect(jsonPath("$.list.length()").value(3))
				.andExpect(jsonPath("$.list[0].sortKey").value(sortKey1))
				.andExpect(jsonPath("$.list[1].sortKey").value(sortKey2))
				.andExpect(jsonPath("$.list[2].sortKey").value(sortKey3));
		
		// List items in descending order
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list/", 
				applicationUuid, namespace, id)
				.param("sortOrder", "DESC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list").isArray())
				.andExpect(jsonPath("$.list[0].sortKey").value(sortKey3))
				.andExpect(jsonPath("$.list[1].sortKey").value(sortKey2))
				.andExpect(jsonPath("$.list[2].sortKey").value(sortKey1));
		
		// List with range
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list/", 
				applicationUuid, namespace, id)
				.param("startKey", sortKey1)
				.param("endKey", sortKey3))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list").isArray())
				.andExpect(jsonPath("$.list.length()").value(2)); // Should not include endKey
		
		// List with limit
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list/", 
				applicationUuid, namespace, id)
				.param("limit", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list").isArray())
				.andExpect(jsonPath("$.list.length()").value(2))
				.andExpect(jsonPath("$.cursor").exists()); // Should have pagination cursor
	}
	
	@Test
	void testDeleteAllListItems() throws Exception {
		// Create multiple list items
		for (int i = 0; i < 3; i++) {
			String sortKey = "item-" + i;
			Map<String, Object> data = Map.of("index", i);
			DataStoreRequest itemRequest = new DataStoreRequest();
			itemRequest.setData(data);
			
			mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}/", 
					applicationUuid, namespace, id, sortKey)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(itemRequest)))
					.andExpect(status().isCreated());
		}
		
		// Delete all items
		mockMvc.perform(delete("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list", 
				applicationUuid, namespace, id))
				.andExpect(status().isNoContent());
		
		// Verify all items are deleted
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list/", 
				applicationUuid, namespace, id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list").isArray())
				.andExpect(jsonPath("$.list.length()").value(0));
	}
	
	@Test
	void testWithClassName() throws Exception {
		request.setClassName("com.example.TestClass");
		
		// Create with className
		mockMvc.perform(post("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/", 
				applicationUuid, namespace, id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());
		
		// Verify className is stored (though not returned in response for now)
		// This would be used in future for deserialization
		mockMvc.perform(get("/rest/api/v1/data/{applicationUuid}/{namespace}/{id}", 
				applicationUuid, namespace, id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").exists());
	}
}