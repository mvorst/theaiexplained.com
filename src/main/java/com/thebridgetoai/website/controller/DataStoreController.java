package com.thebridgetoai.website.controller;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mattvorst.shared.controller.BaseRestController;
import com.thebridgetoai.website.model.DataStoreListResponse;
import com.thebridgetoai.website.model.DataStoreRequest;
import com.thebridgetoai.website.model.DataStoreResponse;
import com.thebridgetoai.website.service.DataStoreService;

@RestController
@RequestMapping("/rest/api/v1/data")
public class DataStoreController extends BaseRestController {
	
	@Autowired
	private DataStoreService dataStoreService;
	
	// Simple Key-Value Operations
	
	@PostMapping("/{applicationUuid}/{namespace}/{id}/")
	public CompletableFuture<ResponseEntity<DataStoreResponse>> createValue(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id,
			@RequestBody DataStoreRequest request) {
		
		return dataStoreService.createValue(applicationUuid, namespace, id, request)
				.thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
				.exceptionally(ex -> {
					if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
						throw new ConflictException("Key already exists");
					}
					throw new RuntimeException(ex);
				});
	}
	
	@PutMapping("/{applicationUuid}/{namespace}/{id}")
	public CompletableFuture<ResponseEntity<DataStoreResponse>> updateValue(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id,
			@RequestBody DataStoreRequest request) {
		
		return dataStoreService.updateValue(applicationUuid, namespace, id, request)
				.thenApply(ResponseEntity::ok)
				.exceptionally(ex -> {
					if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
						throw new NotFoundException("Key not found");
					}
					throw new RuntimeException(ex);
				});
	}
	
	@GetMapping("/{applicationUuid}/{namespace}/{id}")
	public CompletableFuture<ResponseEntity<DataStoreResponse>> getValue(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id) {
		
		return dataStoreService.getDataStore(applicationUuid, namespace, id)
				.thenApply(response -> {
					if (response == null) {
						throw new NotFoundException("Key not found");
					}
					return ResponseEntity.ok(response);
				});
	}
	
	@DeleteMapping("/{applicationUuid}/{namespace}/{id}")
	public CompletableFuture<ResponseEntity<Void>> deleteValue(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id) {
		
		return dataStoreService.deleteValue(applicationUuid, namespace, id)
				.thenApply(v -> ResponseEntity.noContent().<Void>build());
	}
	
	// List Operations
	
	@PostMapping("/{applicationUuid}/{namespace}/{id}/{sortKey}/")
	public CompletableFuture<ResponseEntity<DataStoreResponse>> createListItem(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id,
			@PathVariable String sortKey,
			@RequestBody DataStoreRequest request) {
		
		return dataStoreService.createListItem(applicationUuid, namespace, id, sortKey, request)
				.thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
				.exceptionally(ex -> {
					if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
						throw new ConflictException("Item already exists");
					}
					throw new RuntimeException(ex);
				});
	}
	
	@PutMapping("/{applicationUuid}/{namespace}/{id}/{sortKey}")
	public CompletableFuture<ResponseEntity<DataStoreResponse>> updateListItem(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id,
			@PathVariable String sortKey,
			@RequestBody DataStoreRequest request) {
		
		return dataStoreService.updateListItem(applicationUuid, namespace, id, sortKey, request)
				.thenApply(ResponseEntity::ok)
				.exceptionally(ex -> {
					if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
						throw new NotFoundException("Item not found");
					}
					throw new RuntimeException(ex);
				});
	}
	
	@GetMapping("/{applicationUuid}/{namespace}/{id}/{sortKey}")
	public CompletableFuture<ResponseEntity<DataStoreResponse>> getListItem(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id,
			@PathVariable String sortKey) {
		
		return dataStoreService.getListItem(applicationUuid, namespace, id, sortKey)
				.thenApply(response -> {
					if (response == null) {
						throw new NotFoundException("Item not found");
					}
					return ResponseEntity.ok(response);
				});
	}
	
	@DeleteMapping("/{applicationUuid}/{namespace}/{id}/{sortKey}")
	public CompletableFuture<ResponseEntity<Void>> deleteListItem(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id,
			@PathVariable String sortKey) {
		
		return dataStoreService.deleteListItem(applicationUuid, namespace, id, sortKey)
				.thenApply(v -> ResponseEntity.noContent().<Void>build());
	}
	
	@GetMapping("/{applicationUuid}/{namespace}/{id}/list/")
	public CompletableFuture<ResponseEntity<DataStoreListResponse>> listItems(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id,
			@RequestParam(required = false) String startKey,
			@RequestParam(required = false) String endKey,
			@RequestParam(required = false, defaultValue = "100") Integer limit,
			@RequestParam(required = false, defaultValue = "ASC") String sortOrder,
			@RequestParam(required = false) String cursor) {
		
		return dataStoreService.listItems(applicationUuid, namespace, id, startKey, endKey, limit, sortOrder, cursor)
				.thenApply(ResponseEntity::ok);
	}
	
	@DeleteMapping("/{applicationUuid}/{namespace}/{id}/list")
	public CompletableFuture<ResponseEntity<Void>> deleteAllListItems(
			@PathVariable UUID applicationUuid,
			@PathVariable String namespace,
			@PathVariable String id) {
		
		return dataStoreService.deleteAllListItems(applicationUuid, namespace, id)
				.thenApply(v -> ResponseEntity.noContent().<Void>build());
	}
	
	// Custom exception classes for better error handling
	public static class NotFoundException extends RuntimeException {
		public NotFoundException(String message) {
			super(message);
		}
	}
	
	public static class ConflictException extends RuntimeException {
		public ConflictException(String message) {
			super(message);
		}
	}
	
	// Exception handlers
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
		ErrorResponse error = new ErrorResponse(ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
		ErrorResponse error = new ErrorResponse(ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}
	
	public static class ErrorResponse {
		private String error;
		
		public ErrorResponse(String error) {
			this.error = error;
		}
		
		public String getError() {
			return error;
		}
		
		public void setError(String error) {
			this.error = error;
		}
	}
}