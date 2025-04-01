package com.mattvorst.shared.dao;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.service.AmazonServiceFactory;
import com.mattvorst.shared.util.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputExceededException;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

public class BaseDao {
	private static final Logger log = LoggerFactory.getLogger(BaseDao.class);

	protected SqsAsyncClient sqsAsyncClient;
	protected DynamoDbAsyncClient dynamoDbAsyncClient;
	protected DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

	public BaseDao() {
		sqsAsyncClient = AmazonServiceFactory.getSqsAsyncClient(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
		dynamoDbAsyncClient = AmazonServiceFactory.getDynamoDbAsyncClient(Environment.get(EnvironmentConstants.AWS_DYNAMO_DB_REGION), Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
		dynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(dynamoDbAsyncClient)
				.build();
	}

	public BaseDao(String profile) {
		sqsAsyncClient = AmazonServiceFactory.getSqsAsyncClient(profile);
		dynamoDbAsyncClient = AmazonServiceFactory.getDynamoDbAsyncClient(Environment.get(EnvironmentConstants.AWS_DYNAMO_DB_REGION), profile);
		dynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(dynamoDbAsyncClient)
				.build();
	}

	private void handleThroughputException(ProvisionedThroughputExceededException e, String tableName) {
		log.error(String.format("Provisioned Throughput Exceeded on table %s", tableName), e);
	}
}
