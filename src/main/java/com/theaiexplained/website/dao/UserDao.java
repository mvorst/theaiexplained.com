package com.theaiexplained.website.dao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.util.DynamoDbUtils;
import com.theaiexplained.website.dao.model.user.UserProfile;
import com.theaiexplained.website.dao.model.user.UserProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Component
public class UserDao extends BaseDao {
	public UserDao() { super(); }
	public UserDao(String profile) {super(profile);}

	public CompletableFuture<Void> saveUserProfile(UserProfile userProfile) {
		DynamoDbAsyncTable<UserProfile> table = dynamoDbEnhancedAsyncClient.table(UserProfile.TABLE_NAME, TableSchema.fromBean(UserProfile.class));
		AuthorizationUtils.updateAuditProperties(userProfile);
		return table.putItem(userProfile);
	}

	public CompletableFuture<UserProfile> getUserProfile(UUID userUuid) {
		DynamoDbAsyncTable<UserProfile> table = dynamoDbEnhancedAsyncClient.table(UserProfile.TABLE_NAME, TableSchema.fromBean(UserProfile.class));
		return table.getItem(Key.builder().partitionValue(userUuid.toString()).build());
	}

	// This method is used to get a UserProperties object from the database by userUuid
	public CompletableFuture<UserProperties> getUserProperties(UUID userUuid) {
		DynamoDbAsyncTable<UserProperties> table = dynamoDbEnhancedAsyncClient.table(UserProperties.TABLE_NAME, TableSchema.fromBean(UserProperties.class));
		return table.getItem(Key.builder().partitionValue(userUuid.toString()).build());
	}

	// This method is used to save a UserProperties object to the database
	public CompletableFuture<Void> saveUserProperties(UserProperties userProperties) {
		DynamoDbAsyncTable<UserProperties> table = dynamoDbEnhancedAsyncClient.table(UserProperties.TABLE_NAME, TableSchema.fromBean(UserProperties.class));
		AuthorizationUtils.updateAuditProperties(userProperties);
		return table.putItem(userProperties);
	}

	// This method is used to delete a UserProperties object from the database
	public CompletableFuture<UserProperties> deleteUserProperties(UserProperties userProperties) {
		DynamoDbAsyncTable<UserProperties> table = dynamoDbEnhancedAsyncClient.table(UserProperties.TABLE_NAME, TableSchema.fromBean(UserProperties.class));
		return table.deleteItem(userProperties);
	}
}
