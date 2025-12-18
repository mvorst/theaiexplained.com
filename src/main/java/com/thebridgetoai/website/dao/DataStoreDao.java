package com.thebridgetoai.website.dao;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.thebridgetoai.website.dao.model.user.UserProfile;
import org.springframework.stereotype.Repository;

import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.model.DynamoResultList;
import com.thebridgetoai.website.model.DataStore;
import com.thebridgetoai.website.model.DataStoreList;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Repository
public class DataStoreDao extends BaseDao {

	public DataStoreDao() { super(); }
	public DataStoreDao(String profile) {super(profile);}

	public CompletableFuture<DataStore> getDataStore(UUID applicationUuid, String namespace, String id) {
		DataStore keyItem = new DataStore();
		keyItem.setApplicationUuid(applicationUuid);
		keyItem.setNamespace(namespace);
		keyItem.setId(id);

		GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
				.key(Key.builder().partitionValue(keyItem.getDataKey()).build())
				.build();

		DynamoDbAsyncTable<DataStore> table = dynamoDbEnhancedAsyncClient.table(DataStore.TABLE_NAME, TableSchema.fromBean(DataStore.class));

		return table.getItem(request);
	}
	
	public CompletableFuture<DataStore> saveDataStore(DataStore dataStore) {
		// Set condition to ensure item doesn't already exist
		Expression conditionExpression = Expression.builder()
				.expression("attribute_not_exists(dataKey)")
				.build();
		
		PutItemEnhancedRequest<DataStore> request = PutItemEnhancedRequest.builder(DataStore.class)
				.item(dataStore)
				.conditionExpression(conditionExpression)
				.build();

		DynamoDbAsyncTable<DataStore> table = dynamoDbEnhancedAsyncClient.table(DataStore.TABLE_NAME, TableSchema.fromBean(DataStore.class));

		return table.putItem(request)
				.thenApply(response -> dataStore)
				.exceptionally(ex -> {
					if (ex.getCause() instanceof ConditionalCheckFailedException) {
						throw new RuntimeException("Item already exists", ex);
					}
					throw new RuntimeException(ex);
				});
	}
	
	public CompletableFuture<DataStore> updateDataStore(DataStore dataStore) {
		// Set condition to ensure item exists
		Expression conditionExpression = Expression.builder()
				.expression("attribute_exists(dataKey)")
				.build();
		
		PutItemEnhancedRequest<DataStore> request = PutItemEnhancedRequest.builder(DataStore.class)
				.item(dataStore)
				.conditionExpression(conditionExpression)
				.build();

		DynamoDbAsyncTable<DataStore> table = dynamoDbEnhancedAsyncClient.table(DataStore.TABLE_NAME, TableSchema.fromBean(DataStore.class));

		return table.putItem(request)
				.thenApply(response -> dataStore)
				.exceptionally(ex -> {
					if (ex.getCause() instanceof ConditionalCheckFailedException) {
						throw new RuntimeException("Item not found", ex);
					}
					throw new RuntimeException(ex);
				});
	}
	
	public CompletableFuture<Void> deleteDataStore(UUID applicationUuid, String namespace, String id) {
		DataStore keyItem = new DataStore();
		keyItem.setApplicationUuid(applicationUuid);
		keyItem.setNamespace(namespace);
		keyItem.setId(id);
		
		Key key = Key.builder()
				.partitionValue(keyItem.getDataKey())
				.build();
		
		DeleteItemEnhancedRequest request = DeleteItemEnhancedRequest.builder()
				.key(key)
				.build();

		DynamoDbAsyncTable<DataStore> table = dynamoDbEnhancedAsyncClient.table(DataStore.TABLE_NAME, TableSchema.fromBean(DataStore.class));

		return table.deleteItem(request)
				.thenApply(response -> null);
	}
	
	// DataStoreList operations
	
	public CompletableFuture<DataStoreList> getDataStoreListItem(UUID applicationUuid, String namespace, String id, String sortKey) {
		DataStoreList keyItem = new DataStoreList();
		keyItem.setApplicationUuid(applicationUuid);
		keyItem.setNamespace(namespace);
		keyItem.setId(id);
		keyItem.setSortKey(sortKey);
		
		Key key = Key.builder()
				.partitionValue(keyItem.getDataKey())
				.sortValue(sortKey)
				.build();
		
		GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
				.key(key)
				.build();

		DynamoDbAsyncTable<DataStoreList> table = dynamoDbEnhancedAsyncClient.table(DataStoreList.TABLE_NAME, TableSchema.fromBean(DataStoreList.class));

		return table.getItem(request);
	}
	
	public CompletableFuture<DataStoreList> saveDataStoreListItem(DataStoreList item) {
		Expression conditionExpression = Expression.builder()
				.expression("attribute_not_exists(dataKey)")
				.build();
		
		PutItemEnhancedRequest<DataStoreList> request = PutItemEnhancedRequest.builder(DataStoreList.class)
				.item(item)
				.conditionExpression(conditionExpression)
				.build();

		DynamoDbAsyncTable<DataStoreList> table = dynamoDbEnhancedAsyncClient.table(DataStoreList.TABLE_NAME, TableSchema.fromBean(DataStoreList.class));

		return table.putItem(request)
				.thenApply(response -> item)
				.exceptionally(ex -> {
					if (ex.getCause() instanceof ConditionalCheckFailedException) {
						throw new RuntimeException("Item already exists", ex);
					}
					throw new RuntimeException(ex);
				});
	}
	
	public CompletableFuture<DataStoreList> updateDataStoreListItem(DataStoreList item) {
		Expression conditionExpression = Expression.builder()
				.expression("attribute_exists(dataKey)")
				.build();
		
		PutItemEnhancedRequest<DataStoreList> request = PutItemEnhancedRequest.builder(DataStoreList.class)
				.item(item)
				.conditionExpression(conditionExpression)
				.build();

		DynamoDbAsyncTable<DataStoreList> table = dynamoDbEnhancedAsyncClient.table(DataStoreList.TABLE_NAME, TableSchema.fromBean(DataStoreList.class));

		return table.putItem(request)
				.thenApply(response -> item)
				.exceptionally(ex -> {
					if (ex.getCause() instanceof ConditionalCheckFailedException) {
						throw new RuntimeException("Item not found", ex);
					}
					throw new RuntimeException(ex);
				});
	}
	
	public CompletableFuture<Void> deleteDataStoreListItem(UUID applicationUuid, String namespace, String id, String sortKey) {
		DataStoreList keyItem = new DataStoreList();
		keyItem.setApplicationUuid(applicationUuid);
		keyItem.setNamespace(namespace);
		keyItem.setId(id);
		keyItem.setSortKey(sortKey);
		
		Key key = Key.builder()
				.partitionValue(keyItem.getDataKey())
				.sortValue(sortKey)
				.build();
		
		DeleteItemEnhancedRequest request = DeleteItemEnhancedRequest.builder()
				.key(key)
				.build();

		DynamoDbAsyncTable<DataStoreList> table = dynamoDbEnhancedAsyncClient.table(DataStoreList.TABLE_NAME, TableSchema.fromBean(DataStoreList.class));

		return table.deleteItem(request)
				.thenApply(response -> null);
	}
	
	public CompletableFuture<DynamoResultList<DataStoreList>> getDataStoreListItems(UUID applicationUuid, String namespace, String id,
																					String startKey, String endKey, Integer limit, boolean ascending, Map<String, AttributeValue> lastEvaluatedKey) {
		
		DataStoreList keyItem = new DataStoreList();
		keyItem.setApplicationUuid(applicationUuid);
		keyItem.setNamespace(namespace);
		keyItem.setId(id);
		
		String partitionKey = keyItem.getDataKey();
		
		QueryConditional queryConditional;
		
		// Build query conditional based on sort key range
		if (startKey != null && endKey != null) {
			queryConditional = QueryConditional.sortBetween(
				Key.builder().partitionValue(partitionKey).sortValue(startKey).build(),
				Key.builder().partitionValue(partitionKey).sortValue(endKey).build()
			);
		} else if (startKey != null) {
			queryConditional = QueryConditional.sortGreaterThanOrEqualTo(
				Key.builder().partitionValue(partitionKey).sortValue(startKey).build()
			);
		} else if (endKey != null) {
			queryConditional = QueryConditional.sortLessThan(
				Key.builder().partitionValue(partitionKey).sortValue(endKey).build()
			);
		} else {
			queryConditional = QueryConditional.keyEqualTo(
				Key.builder().partitionValue(partitionKey).build()
			);
		}
		
		QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
				.queryConditional(queryConditional)
				.scanIndexForward(ascending);
		
		if (limit != null) {
			requestBuilder.limit(limit);
		}
		
		if (lastEvaluatedKey != null) {
			requestBuilder.exclusiveStartKey(lastEvaluatedKey);
		}
		
		QueryEnhancedRequest request = requestBuilder.build();
		
		// Execute query and collect first page
		CompletableFuture<DynamoResultList<DataStoreList>> future = new CompletableFuture<>();
		
		List<DataStoreList> allItems = new java.util.ArrayList<>();
		Map<String, AttributeValue>[] lastKeyHolder = new Map[1];

		DynamoDbAsyncTable<DataStoreList> table = dynamoDbEnhancedAsyncClient.table(DataStoreList.TABLE_NAME, TableSchema.fromBean(DataStoreList.class));

		table.query(request)
				.limit(1) // Get just the first page
				.subscribe(page -> {
					allItems.addAll(page.items());
					lastKeyHolder[0] = page.lastEvaluatedKey();
				})
				.handle((result, ex) -> {
					if (ex != null) {
						future.completeExceptionally(ex);
					} else {
						future.complete(new DynamoResultList<>(allItems, lastKeyHolder[0]));
					}
					return null;
				});
		
		return future;
	}
	
	public CompletableFuture<Void> deleteDataStoreListItems(UUID applicationUuid, String namespace, String id) {
		// Query all items with the partition key and delete them
		DataStoreList keyItem = new DataStoreList();
		keyItem.setApplicationUuid(applicationUuid);
		keyItem.setNamespace(namespace);
		keyItem.setId(id);
		
		String partitionKey = keyItem.getDataKey();
		
		QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
				.partitionValue(partitionKey)
				.build());
		
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(queryConditional)
				.build();
		
		CompletableFuture<Void> future = new CompletableFuture<>();
		List<CompletableFuture<Void>> deleteFutures = new java.util.ArrayList<>();

		DynamoDbAsyncTable<DataStoreList> table = dynamoDbEnhancedAsyncClient.table(DataStoreList.TABLE_NAME, TableSchema.fromBean(DataStoreList.class));

		table.query(request)
				.subscribe(page -> {
					for (DataStoreList item : page.items()) {
						Key key = Key.builder()
								.partitionValue(item.getDataKey())
								.sortValue(item.getSortKey())
								.build();
						
						DeleteItemEnhancedRequest deleteRequest = DeleteItemEnhancedRequest.builder()
								.key(key)
								.build();
						
						deleteFutures.add(table.deleteItem(deleteRequest).thenApply(v -> null));
					}
				})
				.handle((result, ex) -> {
					if (ex != null) {
						future.completeExceptionally(ex);
					} else {
						// Wait for all deletes to complete
						CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
								.thenApply(v -> null)
								.whenComplete((v, deleteEx) -> {
									if (deleteEx != null) {
										future.completeExceptionally(deleteEx);
									} else {
										future.complete(null);
									}
								});
					}
					return null;
				});
		
		return future;
	}
}