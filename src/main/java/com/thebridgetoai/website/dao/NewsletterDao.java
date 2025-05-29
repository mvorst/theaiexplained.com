package com.thebridgetoai.website.dao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.util.DynamoDbUtils;
import com.thebridgetoai.website.dao.model.Newsletter;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class NewsletterDao extends BaseDao {

    public CompletableFuture<Newsletter> getNewsletter(UUID newsletterUuid) {
        DynamoDbAsyncTable<Newsletter> table = dynamoDbEnhancedAsyncClient.table(Newsletter.TABLE_NAME, TableSchema.fromBean(Newsletter.class));
        return table.getItem(GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(newsletterUuid.toString()).build())
                .consistentRead(true)
                .build()
        );
    }

    public CompletableFuture<Void> saveNewsletter(Newsletter newsletter) {
        DynamoDbAsyncTable<Newsletter> table = dynamoDbEnhancedAsyncClient.table(Newsletter.TABLE_NAME, TableSchema.fromBean(Newsletter.class));
        AuthorizationUtils.updateAuditProperties(newsletter);
        return table.putItem(newsletter);
    }

    public CompletableFuture<Newsletter> deleteNewsletter(Newsletter newsletter) {
        DynamoDbAsyncTable<Newsletter> table = dynamoDbEnhancedAsyncClient.table(Newsletter.TABLE_NAME, TableSchema.fromBean(Newsletter.class));
        return table.deleteItem(newsletter);
    }

    public CompletableFuture<DynamoResultList<Newsletter>> getAllNewsletterList(int count, Map<String, AttributeValue> attributeValueMap) {
        DynamoDbAsyncTable<Newsletter> table = dynamoDbEnhancedAsyncClient.table(Newsletter.TABLE_NAME, TableSchema.fromBean(Newsletter.class));

        // Build the ScanEnhancedRequest with pagination
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .limit(count)
                .exclusiveStartKey(attributeValueMap)
                .build();

        // Execute the scan
        return DynamoDbUtils.queryWithPagination(table, request);
    }

    public CompletableFuture<DynamoResultList<Newsletter>> getNewsletterListByCreatedDate(int count, Map<String, AttributeValue> attributeValueMap) {
        DynamoDbAsyncTable<Newsletter> table = dynamoDbEnhancedAsyncClient.table(Newsletter.TABLE_NAME, TableSchema.fromBean(Newsletter.class));
        DynamoDbAsyncIndex<Newsletter> index = table.index(Newsletter.GSI_CREATED_DATE_NEWSLETTER_UUID);

        // Build the ScanEnhancedRequest with pagination
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .limit(count)
                .exclusiveStartKey(attributeValueMap)
                .build();

        // Execute the scan on the index
        return DynamoDbUtils.queryWithPagination(table, request);
    }

    public CompletableFuture<DynamoResultList<Newsletter>> getNewsletterListByStatusAndCreatedDate(Status status, int count, Map<String, AttributeValue> attributeValueMap) {
        DynamoDbAsyncTable<Newsletter> table = dynamoDbEnhancedAsyncClient.table(Newsletter.TABLE_NAME, TableSchema.fromBean(Newsletter.class));
        DynamoDbAsyncIndex<Newsletter> index = table.index(Newsletter.GSI_STATUS_CREATED_DATE);

        // Build the QueryEnhancedRequest with pagination
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(status.toString())
                                .build())
                )
                .limit(count)
                .exclusiveStartKey(attributeValueMap)
                .scanIndexForward(false) // Sort in descending order (newest first)
                .build();

        // Execute the query on the index
        return DynamoDbUtils.queryWithPagination(index, request);
    }
}