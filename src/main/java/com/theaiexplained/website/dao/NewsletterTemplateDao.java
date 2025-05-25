package com.theaiexplained.website.dao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.util.DynamoDbUtils;
import com.theaiexplained.website.constant.TemplateCategory;
import com.theaiexplained.website.dao.model.NewsletterTemplate;
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
public class NewsletterTemplateDao extends BaseDao {

    public CompletableFuture<NewsletterTemplate> getTemplate(UUID templateUuid) {
        DynamoDbAsyncTable<NewsletterTemplate> table = dynamoDbEnhancedAsyncClient.table(NewsletterTemplate.TABLE_NAME, TableSchema.fromBean(NewsletterTemplate.class));
        return table.getItem(GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(templateUuid.toString()).build())
                .consistentRead(true)
                .build()
        );
    }

    public CompletableFuture<Void> saveTemplate(NewsletterTemplate template) {
        DynamoDbAsyncTable<NewsletterTemplate> table = dynamoDbEnhancedAsyncClient.table(NewsletterTemplate.TABLE_NAME, TableSchema.fromBean(NewsletterTemplate.class));
        AuthorizationUtils.updateAuditProperties(template);
        return table.putItem(template);
    }

    public CompletableFuture<NewsletterTemplate> deleteTemplate(NewsletterTemplate template) {
        DynamoDbAsyncTable<NewsletterTemplate> table = dynamoDbEnhancedAsyncClient.table(NewsletterTemplate.TABLE_NAME, TableSchema.fromBean(NewsletterTemplate.class));
        return table.deleteItem(template);
    }

    public CompletableFuture<DynamoResultList<NewsletterTemplate>> getAllTemplateList(int count, Map<String, AttributeValue> attributeValueMap) {
        DynamoDbAsyncTable<NewsletterTemplate> table = dynamoDbEnhancedAsyncClient.table(NewsletterTemplate.TABLE_NAME, TableSchema.fromBean(NewsletterTemplate.class));

        // Build the ScanEnhancedRequest with pagination
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .limit(count)
                .exclusiveStartKey(attributeValueMap)
                .build();

        // Execute the scan
        return DynamoDbUtils.queryWithPagination(table, request);
    }

    public CompletableFuture<DynamoResultList<NewsletterTemplate>> getTemplateListByCreatedDate(int count, Map<String, AttributeValue> attributeValueMap) {
        DynamoDbAsyncTable<NewsletterTemplate> table = dynamoDbEnhancedAsyncClient.table(NewsletterTemplate.TABLE_NAME, TableSchema.fromBean(NewsletterTemplate.class));
        DynamoDbAsyncIndex<NewsletterTemplate> index = table.index(NewsletterTemplate.GSI_CREATED_DATE_TEMPLATE_UUID);

        // Build the ScanEnhancedRequest with pagination
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .limit(count)
                .exclusiveStartKey(attributeValueMap)
                .build();

        // Execute the scan on the index
        return DynamoDbUtils.queryWithPagination(table, request);
    }

    public CompletableFuture<DynamoResultList<NewsletterTemplate>> getTemplateListByCategoryAndCreatedDate(TemplateCategory category, int count, Map<String, AttributeValue> attributeValueMap) {
        DynamoDbAsyncTable<NewsletterTemplate> table = dynamoDbEnhancedAsyncClient.table(NewsletterTemplate.TABLE_NAME, TableSchema.fromBean(NewsletterTemplate.class));
        DynamoDbAsyncIndex<NewsletterTemplate> index = table.index(NewsletterTemplate.GSI_CATEGORY_CREATED_DATE);

        // Build the QueryEnhancedRequest with pagination
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(category.toString())
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