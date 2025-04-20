package com.theaiexplained.website.dao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.util.DynamoDbUtils;
import com.theaiexplained.website.dao.model.Content;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Component
public class ContentDao extends BaseDao {
	public ContentDao() { super(); }
	public ContentDao(String profile) { super(profile); }

	public CompletableFuture<Content> getContent(UUID contentUuid) {
		DynamoDbAsyncTable<Content> table = dynamoDbEnhancedAsyncClient.table(Content.TABLE_NAME, TableSchema.fromBean(Content.class));
		return table.getItem(Key.builder().partitionValue(contentUuid.toString()).build());
	}

	public CompletableFuture<Void> saveContent(Content content) {
		DynamoDbAsyncTable<Content> table = dynamoDbEnhancedAsyncClient.table(Content.TABLE_NAME, TableSchema.fromBean(Content.class));
		AuthorizationUtils.updateAuditProperties(content);
		return table.putItem(content);
	}

	public CompletableFuture<Content> deleteContent(Content content) {
		DynamoDbAsyncTable<Content> table = dynamoDbEnhancedAsyncClient.table(Content.TABLE_NAME, TableSchema.fromBean(Content.class));
		return table.deleteItem(content);
	}

	public CompletableFuture<DynamoResultList<Content>> getAllContent(int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<Content> table = dynamoDbEnhancedAsyncClient.table(Content.TABLE_NAME, TableSchema.fromBean(Content.class));

		// Build the ScanEnhancedRequest with pagination
		ScanEnhancedRequest request = ScanEnhancedRequest.builder()
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.build();

		// Execute the scan
		return DynamoDbUtils.queryWithPagination(table, request);
	}

	public CompletableFuture<DynamoResultList<Content>> getContentListByDate(int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<Content> table = dynamoDbEnhancedAsyncClient.table(Content.TABLE_NAME, TableSchema.fromBean(Content.class));
		DynamoDbAsyncIndex<Content> index = table.index("");

		// Build the ScanEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.scanIndexForward(false)
				.build();

		// Execute the scan
		return DynamoDbUtils.queryWithPagination(table, request);
	}
}