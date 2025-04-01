package com.mattvorst.shared.dao;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.dao.model.file.File;
import com.mattvorst.shared.dao.model.file.FileReference;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class FileDao extends BaseDao {

	public FileDao() { super(); }
	public FileDao(String profile) {super(profile);}

	public CompletableFuture<File> getFile(UUID fileUuid) {
		DynamoDbAsyncTable<File> table = dynamoDbEnhancedAsyncClient.table(File.TABLE_NAME, TableSchema.fromBean(File.class));
		return table.getItem(Key.builder().partitionValue(fileUuid.toString()).build());
	}

	public CompletableFuture<Void> saveFile(File file) {
		DynamoDbAsyncTable<File> table = dynamoDbEnhancedAsyncClient.table(File.TABLE_NAME, TableSchema.fromBean(File.class));
		AuthorizationUtils.updateAuditProperties(file);
		return table.putItem(file);
	}

	public CompletableFuture<File> deleteFile(File file) {
		DynamoDbAsyncTable<File> table = dynamoDbEnhancedAsyncClient.table(File.TABLE_NAME, TableSchema.fromBean(File.class));
		return table.deleteItem(file);
	}

	public CompletableFuture<FileReference> getFileReference(UUID referenceUuid) {
		DynamoDbAsyncTable<FileReference> table = dynamoDbEnhancedAsyncClient.table(FileReference.TABLE_NAME, TableSchema.fromBean(FileReference.class));
		return table.getItem(Key.builder().partitionValue(referenceUuid.toString()).build());
	}

	public CompletableFuture<Void> saveFileReference(FileReference fileReference) {
		DynamoDbAsyncTable<FileReference> table = dynamoDbEnhancedAsyncClient.table(FileReference.TABLE_NAME, TableSchema.fromBean(FileReference.class));
		AuthorizationUtils.updateAuditProperties(fileReference);
		return table.putItem(fileReference);
	}
}
