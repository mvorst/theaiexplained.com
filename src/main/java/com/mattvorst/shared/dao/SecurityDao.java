package com.mattvorst.shared.dao;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.dao.model.security.SourceUser;
import com.mattvorst.shared.dao.model.security.User;
import com.mattvorst.shared.dao.model.security.UserPassword;
import com.mattvorst.shared.security.constant.Source;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class SecurityDao extends BaseDao {
	public SecurityDao() { super(); }
	public SecurityDao(String profile) {super(profile);}

	public CompletableFuture<SourceUser> getSourceUser(Source source, String sourceId) {
		DynamoDbAsyncTable<SourceUser> table = dynamoDbEnhancedAsyncClient.table(SourceUser.TABLE_NAME, TableSchema.fromBean(SourceUser.class));
		return table.getItem(Key.builder().partitionValue(source + "|" + sourceId.toUpperCase().trim()).build());
	}

	public CompletableFuture<Void> saveSourceUser(SourceUser sourceUser) {
		DynamoDbAsyncTable<SourceUser> table = dynamoDbEnhancedAsyncClient.table(SourceUser.TABLE_NAME, TableSchema.fromBean(SourceUser.class));
		return table.putItem(sourceUser);
	}

	public CompletableFuture<User> getUser(UUID userUuid) {
		DynamoDbAsyncTable<User> table = dynamoDbEnhancedAsyncClient.table(User.TABLE_NAME, TableSchema.fromBean(User.class));
		return table.getItem(Key.builder().partitionValue(userUuid.toString()).build());
	}

	public CompletableFuture<Void> saveUser(User user) {
		DynamoDbAsyncTable<User> table = dynamoDbEnhancedAsyncClient.table(User.TABLE_NAME, TableSchema.fromBean(User.class));
		return table.putItem(user);
	}

	public CompletableFuture<UserPassword> getUserPassword(UUID passwordUuid, long revision) {
		DynamoDbAsyncTable<UserPassword> table = dynamoDbEnhancedAsyncClient.table(UserPassword.TABLE_NAME, TableSchema.fromBean(UserPassword.class));
		return table.getItem(Key.builder().partitionValue(passwordUuid.toString()).sortValue(revision).build());
	}

	public CompletableFuture<Void> saveUserPassword(UserPassword userPassword) {
		DynamoDbAsyncTable<UserPassword> table = dynamoDbEnhancedAsyncClient.table(UserPassword.TABLE_NAME, TableSchema.fromBean(UserPassword.class));
		return table.putItem(userPassword);
	}
}
