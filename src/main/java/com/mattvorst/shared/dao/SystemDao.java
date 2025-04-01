package com.mattvorst.shared.dao;

import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.constant.PropertyType;
import com.mattvorst.shared.dao.model.system.EC2Instance;
import com.mattvorst.shared.dao.model.system.SystemProperty;
import com.mattvorst.shared.security.AuthorizationUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class SystemDao extends BaseDao {
	public SystemDao() { super(); }
	public SystemDao(String profile) {super(profile);}

	public CompletableFuture<EC2Instance> getEC2Instance(String instanceId) {
		DynamoDbAsyncTable<EC2Instance> table = dynamoDbEnhancedAsyncClient.table(EC2Instance.TABLE_NAME, TableSchema.fromBean(EC2Instance.class));
		return table.getItem(Key.builder().partitionValue(instanceId).build());
	}

	public CompletableFuture<Void> saveEC2Instance(EC2Instance ec2Instance) {
		DynamoDbAsyncTable<EC2Instance> table = dynamoDbEnhancedAsyncClient.table(EC2Instance.TABLE_NAME, TableSchema.fromBean(EC2Instance.class));
		return table.putItem(ec2Instance);
	}

	public CompletableFuture<EC2Instance> getEC2InstanceActive(String instanceId) {
		DynamoDbAsyncTable<EC2Instance> table = dynamoDbEnhancedAsyncClient.table(EC2Instance.TABLE_NAME + "_active", TableSchema.fromBean(EC2Instance.class));
		return table.getItem(Key.builder().partitionValue(instanceId).build());
	}

	public CompletableFuture<Void> saveEC2InstanceActive(EC2Instance ec2Instance) {
		DynamoDbAsyncTable<EC2Instance> table = dynamoDbEnhancedAsyncClient.table(EC2Instance.TABLE_NAME + "_active", TableSchema.fromBean(EC2Instance.class));
		return table.putItem(ec2Instance);
	}

	public CompletableFuture<EC2Instance> deleteEC2InstanceActive(EC2Instance ec2Instance) {
		DynamoDbAsyncTable<EC2Instance> table = dynamoDbEnhancedAsyncClient.table(EC2Instance.TABLE_NAME + "_active", TableSchema.fromBean(EC2Instance.class));
		return table.deleteItem(ec2Instance);
	}

	public CompletableFuture<SystemProperty> getSystemProperty(PropertyType propertyType) {
		DynamoDbAsyncTable<SystemProperty> table = dynamoDbEnhancedAsyncClient.table(SystemProperty.TABLE_NAME, TableSchema.fromBean(SystemProperty.class));
		return table.getItem(Key.builder().partitionValue(propertyType.toString()).build());
	}

	public CompletableFuture<Void> saveSystemProperty(SystemProperty systemProperty) {
		DynamoDbAsyncTable<SystemProperty> table = dynamoDbEnhancedAsyncClient.table(SystemProperty.TABLE_NAME, TableSchema.fromBean(SystemProperty.class));
		AuthorizationUtils.updateAuditProperties(systemProperty);
		return table.putItem(systemProperty);
	}

	public CompletableFuture<SystemProperty> deleteSystemProperty(SystemProperty systemProperty) {
		DynamoDbAsyncTable<SystemProperty> table = dynamoDbEnhancedAsyncClient.table(SystemProperty.TABLE_NAME, TableSchema.fromBean(SystemProperty.class));
		return table.deleteItem(systemProperty);
	}
}
