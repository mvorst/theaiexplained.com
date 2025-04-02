package com.theaiexplained.dao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.util.DynamoDbUtils;
import com.theaiexplained.constant.TaskStatus;
import com.theaiexplained.dao.model.Task;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Component
public class TaskDao extends BaseDao {
	public TaskDao() { super(); }
	public TaskDao(String profile) { super(profile); }

	public CompletableFuture<Task> getTask(UUID taskUuid) {
		DynamoDbAsyncTable<Task> table = dynamoDbEnhancedAsyncClient.table(Task.TABLE_NAME, TableSchema.fromBean(Task.class));
		return table.getItem(Key.builder().partitionValue(taskUuid.toString()).build());
	}

	public CompletableFuture<Void> saveTask(Task task) {
		DynamoDbAsyncTable<Task> table = dynamoDbEnhancedAsyncClient.table(Task.TABLE_NAME, TableSchema.fromBean(Task.class));
		AuthorizationUtils.updateAuditProperties(task);
		return table.putItem(task);
	}

	public CompletableFuture<Task> deleteTask(Task task) {
		DynamoDbAsyncTable<Task> table = dynamoDbEnhancedAsyncClient.table(Task.TABLE_NAME, TableSchema.fromBean(Task.class));
		return table.deleteItem(task);
	}

	public CompletableFuture<DynamoResultList<Task>> getTaskListByUserUuid(UUID userUuid, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<Task> table = dynamoDbEnhancedAsyncClient.table(Task.TABLE_NAME, TableSchema.fromBean(Task.class));
		final DynamoDbAsyncIndex<Task> index = table.index("userUuid-taskUuid-index");

		QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(userUuid.toString()).build());

		// Build the QueryEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(queryConditional)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.build();

		// Execute the query
		return DynamoDbUtils.queryWithPagination(index, request);
	}

	public CompletableFuture<DynamoResultList<Task>> getTaskListByUserUuidAndStatus(UUID userUuid, TaskStatus taskStatus, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<Task> table = dynamoDbEnhancedAsyncClient.table(Task.TABLE_NAME, TableSchema.fromBean(Task.class));
		final DynamoDbAsyncIndex<Task> index = table.index("userUuid-taskStatusCreatedDateAndTaskUuid-index");

		// Create the prefix for our sort key condition (status#)
		String sortKeyPrefix = taskStatus.name() + "|";

		// Build the query conditional for partitionKey = userUuid AND begins_with(sortKey, 'STATUS|')
		QueryConditional queryConditional = QueryConditional.sortBeginsWith(
				Key.builder()
						.partitionValue(userUuid.toString())
						.sortValue(sortKeyPrefix)
						.build());

		// Build the QueryEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(queryConditional)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.scanIndexForward(false) // descending order (newest tasks first)
				.build();

		// Execute the query
		return DynamoDbUtils.queryWithPagination(index, request);
	}
}