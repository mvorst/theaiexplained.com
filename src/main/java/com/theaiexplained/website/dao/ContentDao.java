package com.theaiexplained.website.dao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.util.DynamoDbUtils;
import com.theaiexplained.website.constant.ContentCategoryType;
import com.theaiexplained.website.dao.model.Content;
import com.theaiexplained.website.dao.model.ContentAssociation;
import com.theaiexplained.website.dao.model.FeaturedContent;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
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
		return table.getItem(GetItemEnhancedRequest.builder()
						.key(Key.builder().partitionValue(contentUuid.toString()).build())
				.consistentRead(true)
				.build()
		);
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

	public CompletableFuture<DynamoResultList<Content>> getContentListByDate(ContentCategoryType contentCategoryType, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<Content> table = dynamoDbEnhancedAsyncClient.table(Content.TABLE_NAME, TableSchema.fromBean(Content.class));
		DynamoDbAsyncIndex<Content> index = table.index("contentCategoryType-publishedDateAndContentUuid-index");

		// Build the ScanEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(
						Key.builder()
								.partitionValue(contentCategoryType.toString())
								.build())
				)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.scanIndexForward(false)
				.build();

		// Execute the scan
		return DynamoDbUtils.queryWithPagination(index, request);
	}

	public CompletableFuture<ContentAssociation> getContentAssociation(UUID associationUuid) {
		DynamoDbAsyncTable<ContentAssociation> table = dynamoDbEnhancedAsyncClient.table(ContentAssociation.TABLE_NAME, TableSchema.fromBean(ContentAssociation.class));
		return table.getItem(Key.builder().partitionValue(associationUuid.toString()).build());
	}

	public CompletableFuture<Void> saveContentAssociation(ContentAssociation contentAssociation){
		DynamoDbAsyncTable<ContentAssociation> table = dynamoDbEnhancedAsyncClient.table(ContentAssociation.TABLE_NAME, TableSchema.fromBean(ContentAssociation.class));
		AuthorizationUtils.updateAuditProperties(contentAssociation);
		return table.putItem(contentAssociation);
	}


	public CompletableFuture<ContentAssociation> deleteContentAssociation(ContentAssociation contentAssociation) {
		DynamoDbAsyncTable<ContentAssociation> table = dynamoDbEnhancedAsyncClient.table(ContentAssociation.TABLE_NAME, TableSchema.fromBean(ContentAssociation.class));
		return table.deleteItem(contentAssociation);
	}

	public CompletableFuture<DynamoResultList<ContentAssociation>> getContentAssociationList(int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<ContentAssociation> table = dynamoDbEnhancedAsyncClient.table(ContentAssociation.TABLE_NAME, TableSchema.fromBean(ContentAssociation.class));

		// Build the ScanEnhancedRequest with pagination
		ScanEnhancedRequest request = ScanEnhancedRequest.builder()
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.build();

		// Execute the scan
		return DynamoDbUtils.queryWithPagination(table, request);
	}

	public CompletableFuture<DynamoResultList<ContentAssociation>> getContentAssociationListByAssociatedContentUuid(UUID associatedContentUuid, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<ContentAssociation> table = dynamoDbEnhancedAsyncClient.table(ContentAssociation.TABLE_NAME, TableSchema.fromBean(ContentAssociation.class));
		DynamoDbAsyncIndex<ContentAssociation> index = table.index("associatedContentUuid-publishedDateAndContentUuid-index");

		// Build the ScanEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(
						Key.builder()
								.partitionValue(associatedContentUuid.toString())
								.build())
				)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.scanIndexForward(false)
				.build();

		// Execute the scan
		return DynamoDbUtils.queryWithPagination(index, request);
	}

	public CompletableFuture<DynamoResultList<ContentAssociation>> getContentAssociationListByContentUuid(UUID contentUuid, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<ContentAssociation> table = dynamoDbEnhancedAsyncClient.table(ContentAssociation.TABLE_NAME, TableSchema.fromBean(ContentAssociation.class));
		DynamoDbAsyncIndex<ContentAssociation> index = table.index("contentUuid-associationUuid-index");

		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(
						Key.builder()
								.partitionValue(contentUuid.toString())
								.build())
				)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.scanIndexForward(false)
				.build();

		// Execute the scan
		return DynamoDbUtils.queryWithPagination(index, request);
	}


	// FeaturedContent methods
	public CompletableFuture<FeaturedContent> getFeaturedContent(ContentCategoryType contentCategoryType, UUID contentUuid) {
		DynamoDbAsyncTable<FeaturedContent> table = dynamoDbEnhancedAsyncClient.table(FeaturedContent.TABLE_NAME, TableSchema.fromBean(FeaturedContent.class));
		return table.getItem(GetItemEnhancedRequest.builder()
				.key(Key.builder().partitionValue(contentCategoryType.toString()).sortValue(contentUuid.toString()).build())
				.consistentRead(true)
				.build()
		);
	}

	public CompletableFuture<Void> saveFeaturedContent(FeaturedContent featuredContent) {
		DynamoDbAsyncTable<FeaturedContent> table = dynamoDbEnhancedAsyncClient.table(FeaturedContent.TABLE_NAME, TableSchema.fromBean(FeaturedContent.class));
		AuthorizationUtils.updateAuditProperties(featuredContent);
		return table.putItem(featuredContent);
	}

	public CompletableFuture<FeaturedContent> deleteFeaturedContent(FeaturedContent featuredContent) {
		DynamoDbAsyncTable<FeaturedContent> table = dynamoDbEnhancedAsyncClient.table(FeaturedContent.TABLE_NAME, TableSchema.fromBean(FeaturedContent.class));
		return table.deleteItem(featuredContent);
	}

	public CompletableFuture<DynamoResultList<FeaturedContent>> getAllFeaturedContent(int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<FeaturedContent> table = dynamoDbEnhancedAsyncClient.table(FeaturedContent.TABLE_NAME, TableSchema.fromBean(FeaturedContent.class));

		// Build the ScanEnhancedRequest with pagination
		ScanEnhancedRequest request = ScanEnhancedRequest.builder()
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.build();

		// Execute the scan
		return DynamoDbUtils.queryWithPagination(table, request);
	}

	public CompletableFuture<DynamoResultList<FeaturedContent>> getFeaturedContentByContentCategory(ContentCategoryType contentCategoryType, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<FeaturedContent> table = dynamoDbEnhancedAsyncClient.table(FeaturedContent.TABLE_NAME, TableSchema.fromBean(FeaturedContent.class));

		// Build the QueryEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(
						Key.builder()
								.partitionValue(contentCategoryType.toString())
								.build())
				)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.scanIndexForward(false)
				.build();

		// Execute the query
		return DynamoDbUtils.queryWithPagination(table, request);
	}

	public CompletableFuture<DynamoResultList<FeaturedContent>> getFeaturedContentByCategoryAndDate(ContentCategoryType contentCategoryType, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<FeaturedContent> table = dynamoDbEnhancedAsyncClient.table(FeaturedContent.TABLE_NAME, TableSchema.fromBean(FeaturedContent.class));
		DynamoDbAsyncIndex<FeaturedContent> index = table.index("contentCategoryType-publishedDateAndContentUuid-index");

		// Build the QueryEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(
						Key.builder()
								.partitionValue(contentCategoryType.toString())
								.build())
				)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.scanIndexForward(false) // Sort in descending order (newest first)
				.build();

		// Execute the query
		return DynamoDbUtils.queryWithPagination(index, request);
	}

	public CompletableFuture<DynamoResultList<FeaturedContent>> getFeaturedContentByContentUuid(UUID contentUuid, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
		DynamoDbAsyncTable<FeaturedContent> table = dynamoDbEnhancedAsyncClient.table(FeaturedContent.TABLE_NAME, TableSchema.fromBean(FeaturedContent.class));
		DynamoDbAsyncIndex<FeaturedContent> index = table.index("contentUuid-contentCategoryType-index");

		// Build the QueryEnhancedRequest with pagination
		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(
						Key.builder()
								.partitionValue(contentUuid.toString())
								.build())
				)
				.limit(pageSize)
				.exclusiveStartKey(exclusiveStartKey)
				.build();

		// Execute the query
		return DynamoDbUtils.queryWithPagination(index, request);
	}
}